/**
 * 
 */
package net.ijt.mmorph.strel;

import java.util.TreeMap;

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
public class LocalHistogramDoubleTreeMap
{
    // ==================================================
    // Class variables

    /**
     * The map storing the number of counts for each value in the histogram.
     * 
     * Each count should be strictly greater than 0, and the corresponding key
     * removed if the count is decreased to 0.
     */
    TreeMap<Double, Integer> valueCounts = new TreeMap<>(); 
    
    
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
    public LocalHistogramDoubleTreeMap(int count, double value)
    {
        valueCounts.put(value, count);
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
    }
    
    public double getMaxValue()
    {
        return valueCounts.lastKey();
    }

    public double getMinValue()
    {
        return valueCounts.firstKey();
    }
    
    public void replace(double oldValue, double newValue)
    {
        increaseCount(newValue);
        decreaseCount(oldValue);
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
    }
}
