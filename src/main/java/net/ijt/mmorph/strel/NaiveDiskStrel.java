/**
 * 
 */
package net.ijt.mmorph.strel;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.strel.AbstractStrel;

/**
 * @author dlegland
 *
 */
public class NaiveDiskStrel extends AbstractStrel
{
    // ==================================================
    // Class variables

    /**
     * The radius of the structuring element, in pixels.</p>
     * 
     * A radius of 1 corresponds to a full 3-by-3 square.
     */
    double radius = 1;

    /**
     * The number of pixels around the central pixel.
     */
    int intRadius;
    
    /**
     * An array of shifts referring to strel elements, relative to center pixel.
     */
    int[][] shiftArray;
    
    // ==================================================
    // Constructors

    /**
     * Create a new Disk Strel from its radius.
     * 
     * @param radius
     *            the radius of the disk structuring element, in pixels.
     */
    public NaiveDiskStrel(double radius)
    {
        this.radius = radius;
        this.intRadius = (int) Math.floor(this.radius + 0.5);
        
        createShiftArray();
    }
    
    private void createShiftArray()
    {
        // convert to "real" radius by taking into account central pixel
        double r2 = this.radius + 0.5;
        
        // size of structuring element
        int diam = 2 * this.intRadius + 1;

        // count the number of strel elements
        int count = 0;
        for (int y = 0; y < diam; y++)
        {
            int y2 = y - this.intRadius; 
            for (int x = 0; x < diam; x++)
            {
                int x2 = x - this.intRadius; 
                if (Math.hypot(x2, y2) <= r2)
                {
                    count++;
                }
            }
        }
        
        // create the shift array
        this.shiftArray = new int[count][];
        count = 0;
        for (int y = 0; y < diam; y++)
        {
            int y2 = y - this.intRadius; 
            for (int x = 0; x < diam; x++)
            {
                int x2 = x - this.intRadius; 
                if (Math.hypot(x2, y2) <= r2)
                {
                    this.shiftArray[count++] = new int[] {x2, y2};
                }
            }
        }

    }

    
    // ==================================================
    // Implementation of the Strel2D interface

    /* (non-Javadoc)
     * @see net.sci.image.morphology.Strel2D#dilation(net.sci.array.scalar.ScalarArray2D)
     */
    @Override
    public ImageProcessor dilation(ImageProcessor array)
    {
        if (array instanceof ByteProcessor)
        {
            return dilationUInt8((ByteProcessor) array);
        }
        return dilationFloat(array);
    }
    
    private ByteProcessor dilationUInt8(ByteProcessor array)
    {
        // size of array
        int sizeX = array.getWidth();
        int sizeY = array.getHeight();
        
        ByteProcessor res = new ByteProcessor(sizeX, sizeY);
        
        // iterate over the pixels of the array
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                int value = 0;
                
                // iterate over neighbors
                for (int[] shift : this.shiftArray)
                {
                    int x2 = x + shift[0];
                    int y2 = y + shift[1];
                    if (x2 < 0 || x2 >= sizeX) continue;
                    if (y2 < 0 || y2 >= sizeY) continue;
                    
                    value = Math.max(value, array.get(x2, y2));
                }
                
                res.set(x, y, value);
            }
        }
        
        return res;
    }

    
    private ImageProcessor dilationFloat(ImageProcessor array)
    {
        // size of array
        int sizeX = array.getWidth();
        int sizeY = array.getHeight();
        
        ImageProcessor res = array.duplicate();
        
        // iterate over the pixels of the array
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                float value = 0;
                
                // iterate over neighbors
                for (int[] shift : this.shiftArray)
                {
                    int x2 = x + shift[0];
                    int y2 = y + shift[1];
                    if (x2 < 0 || x2 >= sizeX) continue;
                    if (y2 < 0 || y2 >= sizeY) continue;
                    
                    value = Math.max(value, array.getf(x2, y2));
                }
                
                res.setf(x, y, value);
            }
        }
        
        return res;
    }

    /* (non-Javadoc)
     * @see net.sci.image.morphology.Strel2D#erosion(net.sci.array.scalar.ScalarArray2D)
     */
    @Override
    public ImageProcessor erosion(ImageProcessor array)
    {
        if (array instanceof ByteProcessor)
        {
            return erosionUInt8((ByteProcessor) array);
        }
        return erosionFloat(array);
    }
    
    private ByteProcessor erosionUInt8(ByteProcessor array)
    {
        // size of array
        int sizeX = array.getWidth();
        int sizeY = array.getHeight();
        
        ByteProcessor res = new ByteProcessor(sizeX, sizeY);
        
        // iterate over the pixels of the array
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                int value = 255;
                
                // iterate over neighbors
                for (int[] shift : this.shiftArray)
                {
                    int x2 = x + shift[0];
                    int y2 = y + shift[1];
                    if (x2 < 0 || x2 >= sizeX) continue;
                    if (y2 < 0 || y2 >= sizeY) continue;
                    
                    value = Math.min(value, array.get(x2, y2));
                }
                
                res.set(x, y, value);
            }
        }
        
        return res;
    }

    private ImageProcessor erosionFloat(ImageProcessor array)
    {
        // size of array
        int sizeX = array.getWidth();
        int sizeY = array.getHeight();
        
        ImageProcessor res = array.duplicate();
        
        // iterate over the pixels of the array
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                float value = Float.POSITIVE_INFINITY;
                
                // iterate over neighbors
                for (int[] shift : this.shiftArray)
                {
                    int x2 = x + shift[0];
                    int y2 = y + shift[1];
                    if (x2 < 0 || x2 >= sizeX) continue;
                    if (y2 < 0 || y2 >= sizeY) continue;
                    
                    value = Math.min(value, array.getf(x2, y2));
                }
                
                res.setf(x, y, value);
            }
        }
        
        return res;
    }

    @Override
    public ImageProcessor closing(ImageProcessor array)
    {
        return erosion(dilation(array));
    }

    @Override
    public ImageProcessor opening(ImageProcessor array)
    {
        return dilation(erosion(array));
    }
    
    // ==================================================
    // Implementation of the Strel interface

    @Override
    public int[] getSize() 
    {
        int diam = 2 * this.intRadius + 1;
        return new int[] {diam, diam};
    }

    /* (non-Javadoc)
     * @see net.sci.image.morphology.Strel2D#reverse()
     */
    @Override
    public Strel reverse()
    {
        return this;
    }
    
    /* (non-Javadoc)
     * @see net.sci.image.morphology.Strel2D#getMask()
     */
    @Override
    public int[][] getMask()
    {
        // convert to "real" radius by taking into account central pixel
        double r2 = this.radius + 0.5;
        
        // size of structuring element
        int diam = 2 * this.intRadius + 1;

        // fill the mask
        int[][] mask = new int[diam][diam];
        for (int y = 0; y < diam; y++)
        {
            for (int x = 0; x < diam; x++)
            {
                if (Math.hypot(x - this.intRadius, y - this.intRadius) <= r2)
                {
                    mask[y][x] = 255;
                }
            }
        }
        return mask;
    }
    
    /* (non-Javadoc)
     * @see net.sci.image.morphology.Strel2D#getOffset()
     */
    @Override
    public int[] getOffset()
    {
        return new int[] {this.intRadius, this.intRadius};
    }
    
    /* (non-Javadoc)
     * @see net.sci.image.morphology.Strel2D#getShifts()
     */
    @Override
    public int[][] getShifts()
    {
        return this.shiftArray;
    }
}
