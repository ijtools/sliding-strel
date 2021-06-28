/**
 * 
 */
package net.ijt.mmorph.strel;

import ij.ImageStack;
import inra.ijpb.data.image.ByteStackWrapper;
import inra.ijpb.data.image.Image3D;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.Strel3D;
import inra.ijpb.morphology.strel.AbstractStrel3D;

/**
 * Naive implementation of 3D ball structuring element.
 * 
 * Used as reference implementation for evaluating Sliding structuring element.
 * 
 * @see SlidingBallStrel3D
 * @see NaiveDiskStrel
 * 
 * @author dlegland
 *
 */
public class NaiveBallStrel3D extends AbstractStrel3D
{
    // ==================================================
    // Class variables

    /**
     * The radius of the structuring element, in pixels.</p>
     */
    double radius;

    /**
     * The number of pixels around the central pixel. Used to determine the size
     * of the structuring element.
     */
    int intRadius;
    
    /**
     * An array of shifts referring to strel elements, relative to center pixel.
     * Used for lazy evaluation of getShifts() method. 
     */
    int[][] shiftArray;
    
    int[] xOffsets;
    int[] yOffsets;
    int[] zOffsets;

    
    // ==================================================
    // Constructors

    /**
     * Create a new Ball-shaped structuring element from its radius.
     * 
     * @param radius
     *            the radius of the ball, in voxels.
     */
    public NaiveBallStrel3D(double radius)
    {
        this.radius = radius;
        
        initStrel();
        createShiftArray();
    }
    
    private void initStrel()
    {
        this.intRadius = (int) Math.floor(this.radius + 0.5);

        // compute x-offsets of the XY-projection,
        // that corresponds to the z offsets of the YZ projection
        int[] yOffsets2d = computeOffsets2d(this.intRadius);
        
        // compute total number of run lengths in the x direction
        int nOffsets = 0;
        for (int offset : yOffsets2d)
        {
            nOffsets += (2 * offset + 1);
        }
        
        // allocate arrays
        this.xOffsets = new int[nOffsets];
        this.yOffsets = new int[nOffsets];
        this.zOffsets = new int[nOffsets];
        
        // shortcut for square radius
        double r2 = (this.radius + 0.5) * ((this.radius + 0.5));
        
        // iterate over y offsets and increment zy-offset index when appropriate
        int iOffset = 0;
        for (int iz = 0; iz < yOffsets2d.length; iz++)
        {
            int dz = iz - this.intRadius;
            
            int ny = 2 * yOffsets2d[iz] + 1;
            for (int iy = 0; iy < ny; iy++)
            {
                int dy = iy - yOffsets2d[iz];
                int dx = (int) Math.floor(Math.sqrt(r2 - dy * dy - dz * dz));
                
                this.xOffsets[iOffset] = dx;
                this.yOffsets[iOffset] = dy;
                this.zOffsets[iOffset] = dz;
                iOffset++;
            }
        }
    }
    
    /**
     * @return the set of offset in x for each y-offset. The array length is
     *         odd, corresponding to 2*intRadius+1.
     */
    private final static int[] computeOffsets2d(double radius)
    {
        int intRadius = (int) Math.floor(radius + 0.5);
        // allocate arrays
        int nOffsets = 2 * intRadius + 1;
        int[] xOffsets = new int[nOffsets];
        
        // initialize each row
        double r2 = (radius + 0.5) * ((radius + 0.5));
        for (int i = 0; i < nOffsets; i++)
        {
            int dy = i - intRadius;
            xOffsets[i] = (int) Math.floor(Math.sqrt(r2 - dy * dy));
        }
        
        return xOffsets;
    }
    



    // ==================================================
    // Processing methods
    
    @Override
    public ImageStack dilation(ImageStack image)
    {
        if (image.getBitDepth() == 8)
        {
            return dilationUInt8(image);
        }
        return dilationDouble(image);
    }
    
    private ImageStack dilationUInt8(ImageStack stack)
    {
        ByteStackWrapper array = new ByteStackWrapper(stack);
        
        // get array size
        int sizeX = array.getSize(0);
        int sizeY = array.getSize(1);
        int sizeZ = array.getSize(2);
        
        // Allocate result
        ImageStack resStack = ImageStack.create(sizeX, sizeY, sizeZ, 8);
        ByteStackWrapper res = new ByteStackWrapper(resStack);
        
        // Iterate on image rows indexed by z and y
        for (int z = 0; z < sizeZ; z++)
        {
            fireProgressChanged(this, z, sizeZ);
            
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    int value = array.get(x, y, z);
                    
                    // iterate over neighbors
                    for (int[] shift : this.shiftArray)
                    {
                        int x2 = x + shift[0];
                        int y2 = y + shift[1];
                        int z2 = z + shift[2];
                        if (x2 < 0 || x2 >= sizeX) continue;
                        if (y2 < 0 || y2 >= sizeY) continue;
                        if (z2 < 0 || z2 >= sizeZ) continue;
                        
                        value = Math.max(value, array.get(x2, y2, z2));
                    }

                    res.set(x, y, z, value);
                }
            }
        }

        // clear the progress bar
        fireProgressChanged(this, sizeZ, sizeZ);
        
        return resStack;
    }

    private ImageStack dilationDouble(ImageStack stack)
    {
        Image3D array = Images3D.createWrapper(stack);
        
        // get array size
        int sizeX = array.getSize(0);
        int sizeY = array.getSize(1);
        int sizeZ = array.getSize(2);
        
        // Allocate result
        ImageStack resStack = stack.duplicate();
        Image3D res = Images3D.createWrapper(resStack);
        
        // Iterate on image rows indexed by z and y
        for (int z = 0; z < sizeZ; z++)
        {
            fireProgressChanged(this, z, sizeZ);
            
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    double value = array.getValue(x, y, z);
                    
                    // iterate over neighbors
                    for (int[] shift : this.shiftArray)
                    {
                        int x2 = x + shift[0];
                        int y2 = y + shift[1];
                        int z2 = z + shift[2];
                        if (x2 < 0 || x2 >= sizeX) continue;
                        if (y2 < 0 || y2 >= sizeY) continue;
                        if (z2 < 0 || z2 >= sizeZ) continue;
                        
                        value = Math.max(value, array.getValue(x2, y2, z2));
                    }

                    res.setValue(x, y, z, value);
                }
            }
        }

        // clear the progress bar
        fireProgressChanged(this, sizeZ, sizeZ);
        
        return resStack;
    }

    @Override
    public ImageStack erosion(ImageStack image)
    {
        if (image.getBitDepth() == 8)
        {
            return erosionUInt8(image);
        }
        return erosionDouble(image);
    }
    
    private ImageStack erosionUInt8(ImageStack stack)
    {
        ByteStackWrapper array = new ByteStackWrapper(stack);
        
        // get array size
        int sizeX = array.getSize(0);
        int sizeY = array.getSize(1);
        int sizeZ = array.getSize(2);
        
        // Allocate result
        ImageStack resStack = ImageStack.create(sizeX, sizeY, sizeZ, 8);
        ByteStackWrapper res = new ByteStackWrapper(resStack);

        // Iterate on image rows indexed by z and y
        for (int z = 0; z < sizeZ; z++)
        {
            fireProgressChanged(this, z, sizeZ);
            
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    int value = array.get(x, y, z);
                    
                    // iterate over neighbors
                    for (int[] shift : this.shiftArray)
                    {
                        int x2 = x + shift[0];
                        int y2 = y + shift[1];
                        int z2 = z + shift[2];
                        if (x2 < 0 || x2 >= sizeX) continue;
                        if (y2 < 0 || y2 >= sizeY) continue;
                        if (z2 < 0 || z2 >= sizeZ) continue;
                        
                        value = Math.min(value, array.get(x2, y2, z2));
                    }

                    res.set(x, y, z, value);
                }
            }
        }

        // clear the progress bar
        fireProgressChanged(this, sizeZ, sizeZ);
        
        return resStack;
    }

    private ImageStack erosionDouble(ImageStack stack)
    {
        Image3D array = Images3D.createWrapper(stack);
        
        // get array size
        int sizeX = array.getSize(0);
        int sizeY = array.getSize(1);
        int sizeZ = array.getSize(2);
        
        // Allocate result
        ImageStack resStack = stack.duplicate();
        Image3D res = Images3D.createWrapper(resStack);
        
        // Iterate on image rows indexed by z and y
        for (int z = 0; z < sizeZ; z++)
        {
            fireProgressChanged(this, z, sizeZ);
            
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    double value = array.getValue(x, y, z);
                    
                    // iterate over neighbors
                    for (int[] shift : this.shiftArray)
                    {
                        int x2 = x + shift[0];
                        int y2 = y + shift[1];
                        int z2 = z + shift[2];
                        if (x2 < 0 || x2 >= sizeX) continue;
                        if (y2 < 0 || y2 >= sizeY) continue;
                        if (z2 < 0 || z2 >= sizeZ) continue;
                        
                        value = Math.min(value, array.getValue(x2, y2, z2));
                    }

                    res.setValue(x, y, z, value);
                }
            }
        }

        // clear the progress bar
        fireProgressChanged(this, sizeZ, sizeZ);
        
        return resStack;
    }


    // ==================================================
    // Implementation of Strel3D

    @Override
    public int[] getSize()
    {
        int diam = 2 * this.intRadius + 1;
        return new int[] {diam, diam, diam};
    }

    @Override
    public int[][][] getMask3D()
    {
        // convert to "real" radius by taking into account central pixel
        double r2 = this.radius + 0.5;
        
        // size of structuring element
        int diam = 2 * this.intRadius + 1;

        // fill the mask
        int[][][] mask = new int[diam][diam][diam];
        for (int z = 0; z < diam; z++)
        {
            for (int y = 0; y < diam; y++)
            {
                double y2 = Math.hypot(z - this.intRadius, y - this.intRadius);
                for (int x = 0; x < diam; x++)
                {
                    if (Math.hypot(x - this.intRadius, y2) <= r2)
                    {
                        mask[z][y][x] = 255;
                    }
                }
            }
        }
        return mask;
    }

    @Override
    public int[] getOffset()
    {
        return new int[] {this.intRadius, this.intRadius, this.intRadius};
    }

    @Override
    public int[][] getShifts3D()
    {
        if (this.shiftArray == null)
        {
            createShiftArray();
        }
        return this.shiftArray;
    }

    @Override
    public Strel3D reverse()
    {
        return this;
    }


    // ==================================================
    // Utility methods

    private void createShiftArray()
    {
        int count = elementCount();
        
        // create the shift array
        this.shiftArray = new int[count][];
        count = 0;
    
        int nOffsets = this.xOffsets.length;
        for (int i = 0; i < nOffsets; i++)
        {
            int dz = this.zOffsets[i];
            int dy = this.yOffsets[i];
            int ri = this.xOffsets[i];
            for (int dx = -ri; dx <= ri; dx++)
            {
                this.shiftArray[count++] = new int[] {dx, dy, dz};
            }
        }
    }

    /**
     * @return the number of non zero elements within this structuring element.
     */
    private int elementCount()
    {
        int count = 0;
        for (int i = 0; i < this.xOffsets.length; i++)
        {
            count += 2 * this.xOffsets[i] + 1;
        }
        return count;
    }
    

}
