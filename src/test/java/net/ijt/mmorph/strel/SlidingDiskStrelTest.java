/**
 * 
 */
package net.ijt.mmorph.strel;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;

/**
 * @author dlegland
 *
 */
public class SlidingDiskStrelTest
{

    /**
     * Test method for {@link net.ijt.mmorph.strel.SlidingDiskStrel#dilation(ij.process.ImageProcessor)}.
     */
    @Test
    public final void testDilationImageProcessor()
    {
        // create empty image with a single bright pixel
        ByteProcessor array = new ByteProcessor(30, 30);
        array.set(15, 15, 200);
        
        // create the strel
        Strel strel = new SlidingDiskStrel(10.0);
        
        // compute closing
        ImageProcessor result = strel.dilation(array);
        
        // check values
        assertTrue(result instanceof ByteProcessor);
        assertEquals(200, result.get(15, 15));
        assertEquals(200, result.get( 5, 15));
        assertEquals(200, result.get(25, 15));
        assertEquals(200, result.get(15,  5));
        assertEquals(200, result.get(15, 25));
        assertEquals(  0, result.get( 5,  5));
        assertEquals(  0, result.get(25,  5));
        assertEquals(  0, result.get( 5, 25));
        assertEquals(  0, result.get(25, 25));
    }

    /**
     * Test method for {@link net.ijt.mmorph.strel.SlidingDiskStrel#erosion(ij.process.ImageProcessor)}.
     */
    @Test
    public final void testErosionImageProcessor()
    {
        // create empty white image with a single dark pixel
        ByteProcessor array = new ByteProcessor(30, 30);
        array.setValue(255.0);
        array.fill();
        array.set(15, 15, 50);
        
        // create the strel
        Strel strel = new SlidingDiskStrel(10.0);
        
        // compute closing
        ImageProcessor result = strel.erosion(array);
        
        // check values
        assertTrue(result instanceof ByteProcessor);
        assertEquals( 50, result.get(15, 15));
        assertEquals( 50, result.get( 5, 15));
        assertEquals( 50, result.get(25, 15));
        assertEquals( 50, result.get(15,  5));
        assertEquals( 50, result.get(15, 25));
        assertEquals(255, result.get( 5,  5));
        assertEquals(255, result.get(25,  5));
        assertEquals(255, result.get( 5, 25));
        assertEquals(255, result.get(25, 25));
    }

    /**
     * Test method for {@link net.ijt.mmorph.strel.SlidingDiskStrel#closing(ij.process.ImageProcessor)}.
     */
    @Test
    public final void testClosingImageProcessor()
    {
        // create empty image with a single bright pixel
        ByteProcessor array = new ByteProcessor(30, 30);
        array.set(15, 15, 200);

        // create the strel
        Strel strel = new SlidingDiskStrel(10.0);

        // compute closing
        ImageProcessor result = strel.closing(array);

        // check values
        assertTrue(result instanceof ByteProcessor);
        assertEquals(200, result.get(15, 15));
        assertEquals(  0, result.get(14, 15));
        assertEquals(  0, result.get(16, 15));
        assertEquals(  0, result.get(15, 14));
        assertEquals(  0, result.get(15, 16));
    }

    /**
     * Test method for {@link net.ijt.mmorph.strel.SlidingDiskStrel#opening(ij.process.ImageProcessor)}.
     */
    @Test
    public final void testOpeningImageProcessor()
    {
        // create empty white image with a single dark pixel
        ByteProcessor array = new ByteProcessor(30, 30);
        array.setValue(255.0);
        array.fill();
        array.set(15, 15, 50);
        
        // create the strel
        Strel strel = new SlidingDiskStrel(10.0);
        
        // compute closing
        ImageProcessor result = strel.opening(array);
        
        // check values
        assertTrue(result instanceof ByteProcessor);
        assertEquals( 50, result.get(15, 15));
        assertEquals(255, result.get(14, 15));
        assertEquals(255, result.get(16, 15));
        assertEquals(255, result.get(15, 14));
        assertEquals(255, result.get(15, 16));
    }

}
