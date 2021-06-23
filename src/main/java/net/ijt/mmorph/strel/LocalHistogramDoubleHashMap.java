/**
 * 
 */
package net.ijt.mmorph.strel;

import java.util.HashMap;

/**
 * <p>
 * Keeps an histogram of values within the neighborhood of a position by storing
 * the counts of values within a map indexed by values.
 * </p>
 * 
 * <p>
 * This implementation does not use any buffer, but requires updates to replace
 * old value by new value. Local histogram is stored in a tree map array,
 * resulting in O(log n) complexity for retrieving value counts.
 * </p>
 * 
 * @see LocalHistogramUInt8
 * @see SlidingDiskStrel
 * @see SlidingBallStrel3D
 * 
 * @author dlegland
 *
 */
public class LocalHistogramDoubleHashMap
{
    // ==================================================
    // Class variables

    /**
     * The map storing the number of counts for each value in the histogram.
     * 
     * Each count should be strictly greater than 0, and the corresponding key
     * removed if the count is decreased to 0.
     */
    HashMap<Double, Integer> valueCounts; 
    
    /**
     * The current maximum value, updated only when required.
     */
    double maxValue = Double.POSITIVE_INFINITY;

    /**
     * The flag indicating that the maximum value needs to be recomputed.
     */
    boolean needUpdateMax = false;
    
    /**
     * The current minimum value, updated only when required.
     */
    double minValue = Double.NEGATIVE_INFINITY;
    
    /**
     * The flag indicating that the minimum value needs to be recomputed.
     */
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
    public LocalHistogramDoubleHashMap(int count, double value)
    {
        valueCounts = new HashMap<Double, Integer>((int) (count * 1.4), 0.75f);
        
        reset(count, value);
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
    public void reset(int count, double value)
    {
        valueCounts.clear();
        valueCounts.put(value, count);
        
        this.maxValue = value;
        this.minValue = value;
        this.needUpdateMax = false;
        this.needUpdateMin = false;
    }
    
    public double getMaxValue()
    {
        if (needUpdateMax)
        {
            this.maxValue = Double.NEGATIVE_INFINITY;
            for (double key : valueCounts.keySet())
            {
                this.maxValue = Math.max(this.maxValue, key);
            }
            
            needUpdateMax = false;
        }
        
        return maxValue;
    }

    public double getMinValue()
    {
        if (needUpdateMin)
        {
            this.minValue = Double.POSITIVE_INFINITY;
            for (double key : valueCounts.keySet())
            {
                this.minValue = Math.min(this.minValue, key);
            }
            
            needUpdateMin = false;
        }
        
        return minValue;
    }
    
    public void replace(double oldValue, double newValue)
    {
        if (newValue != oldValue)
        {
            increaseCount(newValue);
            decreaseCount(oldValue);
        }
    }
    
    private void decreaseCount(double value)
    {
        if (valueCounts.containsKey(value))
        {
            // decrease current count
            int count = valueCounts.get(value) - 1;
            if (count > 0)
            {
                valueCounts.put(value, count);
            }
            else
            {
                valueCounts.remove(value);

                // check if necessary to update min/max values 
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

    private void increaseCount(double value)
    {
        if (valueCounts.containsKey(value))
        {
            // increase current count
            valueCounts.put(value, valueCounts.get(value) + 1);
        }
        else
        {
            // create new count
            valueCounts.put(value, 1);
        }

        if (value > maxValue)
        {
            maxValue = value;
            needUpdateMax = false;
        }
        if (value < minValue)
        {
            minValue = value;
            needUpdateMin = false;
        }
    }
}
