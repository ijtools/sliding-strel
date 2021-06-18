/**
 * 
 */
package net.ijt.mmorph.strel;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;
import inra.ijpb.morphology.strel.AbstractStrel;

/**
 * <pre>{@code
    // Creates a disk structuring element with radius 6
    Strel2D strel = new SlidingDiskStrel(6);
    
    // Creates a simple array with white dot in the middle
    UInt8Array2D array = UInt8Array2D.create(15, 15);
    array.setValue(7, 7, 255);
    
    // applies dilation on array
    ScalarArray2D<?> dilated = strel.dilation(array);
    
    // display result
    dilated.print(System.out);
 * }</pre>
 * @author dlegland
 *
 */
public class SlidingDiskStrel extends AbstractStrel implements Strel
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
    
    
    // ==================================================
    // Constructors

    /**
     * Create a new Disk Strel from its radius.
     * 
     * @param radius
     *            the radius of the disk structuring element, in pixels.
     */
    public SlidingDiskStrel(double radius)
    {
        this.radius = radius;
        
        initStrel();
        createShiftArray();
    }
    
    private void initStrel()
    {
        this.intRadius = (int) Math.floor(this.radius + 0.5);
        
        // allocate arrays
        int nOffsets = 2 * this.intRadius + 1;
        this.xOffsets = new int[nOffsets];
        this.yOffsets = new int[nOffsets];
        
        // initialize each row
        double r2 = (this.radius + 0.5) * ((this.radius + 0.5));
        for (int i = 0; i < nOffsets; i++)
        {
            int dy = i - intRadius;
            this.yOffsets[i] = dy; 
            this.xOffsets[i] =  (int) Math.floor(Math.sqrt(r2 - dy * dy));
        }
    }
    

    // ==================================================
    // Specific methods
    
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
    
    
    // ==================================================
    // Implementation of the Strel interface

    @Override
    public ImageProcessor dilation(ImageProcessor array)
    {
        if (array instanceof ByteProcessor)
        {
            return slidingDilationUInt8((ByteProcessor) array);
        }
        throw new RuntimeException("Requires array to be a ByteProcessor");
    }
    
    private ByteProcessor slidingDilationUInt8(ByteProcessor array)
    {
        // get array size
        int sizeX = array.getWidth();
        int sizeY = array.getHeight();
        
        // number of non zero elements 
        int count = elementCount();
        int nOffsets = this.xOffsets.length;
        
        // create local histogram instance
        final int OUTSIDE = 0;
        LocalHistogramUInt8 localHisto = new LocalHistogramUInt8(count, OUTSIDE);

        // Allocate result
        ByteProcessor res = (ByteProcessor) array.duplicate();
        
        // temp variables for updating local histogram
        int vOld, vNew;
        
        // Iterate on image rows indexed by y
        for (int y = 0; y < sizeY; y++)
        {
            fireProgressChanged(this, y, sizeY);
            
            // init local histogram with background values
            localHisto.reset(count, OUTSIDE);

            // update initialization with visible neighbors
            for (int x = -intRadius; x < 0; x++)
            {
                // iterate over the list of offsets
                for (int i = 0; i < nOffsets; i++)
                {
                    int y2 = y + this.yOffsets[i];
                    if (y2 < 0 || y2 >= sizeY)
                    {
                        continue;
                    }

                    int x2 = x + this.xOffsets[i];
                    if (x2 < 0 || x2 >= sizeX)
                    {
                        continue;
                    }
                    localHisto.replace(OUTSIDE, array.get(x2, y2));
                }
            }   

            // iterate along "middle" values
            for (int x = 0; x < sizeX; x++)
            {
                // iterate over the list of offsets
                for (int i = 0; i < nOffsets; i++)
                {
                    // current line offset
                    int y2 = y + this.yOffsets[i];

                    // We need to test values only for lines within array bounds
                    if (y2 >= 0 && y2 < sizeY)
                    {
                        // old value
                        int x2 = x - this.xOffsets[i] - 1;
                        vOld = (x2 >= 0 && x2 < sizeX) ? array.get(x2, y2) : OUTSIDE;

                        // new value
                        x2 = x + this.xOffsets[i];
                        vNew = (x2 >= 0 && x2 < sizeX) ? array.get(x2, y2) : OUTSIDE;

                        localHisto.replace(vOld, vNew);
                    }
                }

                res.set(x, y, (int) localHisto.getMaxValue());
            }
        }

        // clear the progress bar
        fireProgressChanged(this, sizeY, sizeY);

        return res;
    }

    @Override
    public ImageProcessor erosion(ImageProcessor array)
    {
        if (array instanceof ByteProcessor)
        {
            return slidingErosionUInt8((ByteProcessor) array);
        }
        throw new RuntimeException("Requires array to be a ByteProcessor");
    }

    private ByteProcessor slidingErosionUInt8(ByteProcessor array)
    {
        // get array size
        int sizeX = array.getWidth();
        int sizeY = array.getHeight();
        
        // number of non zero elements 
        int count = elementCount();
        int nOffsets = this.xOffsets.length;
        
        // create local histogram instance
        final int OUTSIDE = 255;
        LocalHistogramUInt8 localHisto = new LocalHistogramUInt8(count, OUTSIDE);

        // Allocate result
        ByteProcessor res = (ByteProcessor) array.duplicate();
        
        // temp variables for updating local histogram
        int vOld, vNew;
        
        // Iterate on image rows indexed by y
        for (int y = 0; y < sizeY; y++)
        {
            fireProgressChanged(this, y, sizeY);

            // init local histogram with background values
            localHisto.reset(count, OUTSIDE);

            // update initialization with visible neighbors
            for (int x = -intRadius; x < 0; x++)
            {
                // iterate over the list of offsets
                for (int i = 0; i < nOffsets; i++)
                {
                    int y2 = y + this.yOffsets[i];
                    if (y2 < 0 || y2 >= sizeY)
                    {
                        continue;
                    }

                    int x2 = x + this.xOffsets[i];
                    if (x2 < 0 || x2 >= sizeX)
                    {
                        continue;
                    }
                    localHisto.replace(OUTSIDE, array.get(x2, y2));
                }
            }   

            // iterate along "middle" values
            for (int x = 0; x < sizeX; x++)
            {
                // iterate over the list of offsets
                for (int i = 0; i < nOffsets; i++)
                {
                    // current line offset
                    int y2 = y + this.yOffsets[i];

                    // We need to test values only for lines within array bounds
                    if (y2 >= 0 && y2 < sizeY)
                    {
                        // old value
                        int x2 = x - this.xOffsets[i] - 1;
                        vOld = (x2 >= 0 && x2 < sizeX) ? array.get(x2, y2) : OUTSIDE;

                        // new value
                        x2 = x + this.xOffsets[i];
                        vNew = (x2 >= 0 && x2 < sizeX) ? array.get(x2, y2) : OUTSIDE;

                        localHisto.replace(vOld, vNew);
                    }
                }

                res.set(x, y, (int) localHisto.getMinValue());
            }
        }

        // clear the progress bar
        fireProgressChanged(this, sizeY, sizeY);
        
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
        if (this.shiftArray == null)
        {
            createShiftArray();
        }
        return this.shiftArray;
    }

    private void createShiftArray()
    {
        int count = elementCount();
        
        // create the shift array
        this.shiftArray = new int[count][];
        count = 0;

        int nOffsets = this.xOffsets.length;
        for (int i = 0; i < nOffsets; i++)
        {
            int dy = this.yOffsets[i];
            int ri = this.xOffsets[i];
            for (int dx = -ri; dx <= ri; dx++)
            {
                this.shiftArray[count++] = new int[] {dx, dy};
            }
        }
    }

    
    // ==================================================
    // Implementation of the Strel interface

	@Override
	public int[] getSize() 
	{
        int diam = 2 * this.intRadius + 1;
        return new int[] {diam, diam};
	}

	/**
	 * @return this structuring element, as sliding disk structuring elements are
	 *         symmetric by definition.
	 */
	@Override
	public Strel reverse() 
	{
		return this;
	}
}
