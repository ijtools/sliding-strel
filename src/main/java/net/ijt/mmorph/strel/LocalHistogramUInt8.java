/**
 * 
 */
package net.ijt.mmorph.strel;

/**
 * <p>
 * Keeps an histogram of values within the neighborhood of a position by storing
 * the counts of values within an array of integers.
 * </p>
 * s
 * <p>
 * This implementation does not use any buffer, but requires updates to replace
 * old value by new value.
 * </p>
 * 
 * @author dlegland
 *
 */
public class LocalHistogramUInt8
{
    // ==================================================
    // Class variables

    /**
     * An array to store the count of each value between 0 and 255.
     */
    int[] valueCounts;    
    
    int maxValue = 0;
    int minValue = 255;
    boolean needUpdateMax = false;
    boolean needUpdateMin = false;
    
    
    // ==================================================
    // Constructors

    /**
     * Constructor from histogram size and filling value.
     * 
     * @param count
     *            the number of values within the histogram
     * @param value
     *            the value that fills the histogram.
     */
    public LocalHistogramUInt8(int count, int value)
    {
        this.valueCounts = new int[256];
        this.valueCounts[value] = count;

        this.maxValue = value;
        this.minValue = value;
    }
    
    
    // ==================================================
    // Class methods

    /**
     * Resets this local histogram by filling with the specified value, avoiding
     * to create a new instance.
     * 
     * @param count
     *            the number of values within the histogram
     * @param value
     *            the value that fills the histogram.
     */
    public void reset(int count, int value)
    {
        for (int i = 0; i < 256; i++)
        {
            this.valueCounts[i] = 0;
        }
        this.valueCounts[value] = count;
        
        this.maxValue = value;
        this.minValue = value;
        this.needUpdateMax = false;
        this.needUpdateMin = false;
    }
    
    public double getMaxValue()
    {
        if (needUpdateMax)
        {
            for (maxValue = 255; maxValue > 0; maxValue--)
            {
                if (valueCounts[maxValue] > 0)
                {
                    break;
                }
            }
            needUpdateMax = false;
        }
        return maxValue;
    }

    public double getMinValue()
    {
        if (needUpdateMin)
        {
            for (minValue = 0; minValue < 256; minValue++)
            {
                if (valueCounts[minValue] > 0)
                {
                    break;
                }
            }
            needUpdateMin = false;
        }
        return minValue;
    }
    
    public void replace(int oldValue, int newValue)
    {
        increaseCount(newValue);
        decreaseCount(oldValue);
    }
    
    private void decreaseCount(int value)
    {
        if (valueCounts[value] > 0)
        {
            // decrease current count
            int count = valueCounts[value] - 1;
            valueCounts[value] = count;
            
            if (count == 0)
            {
                if (value == maxValue)
                {
                    needUpdateMax = true;
                }
                if (value == minValue)
                {
                    needUpdateMin = true;
                }
            }
        }
        else
        {
            throw new RuntimeException("Local histogram does not contain count for value " + value);
        }
    }

    private void increaseCount(int value)
    {
        valueCounts[value] = valueCounts[value] + 1;
        if (value > maxValue)
        {
            needUpdateMax = true;
        }
        if (value < minValue)
        {
            needUpdateMin = true;
        }
    }
}
