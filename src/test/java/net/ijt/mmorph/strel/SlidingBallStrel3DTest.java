/**
 * 
 */
package net.ijt.mmorph.strel;

import static org.junit.Assert.*;

import org.junit.Test;

import ij.ImageStack;
import inra.ijpb.data.image.Images3D;
import inra.ijpb.morphology.Strel3D;

/**
 * @author dlegland
 *
 */
public class SlidingBallStrel3DTest
{

    /**
     * Test method for {@link net.ijt.mmorph.strel.SlidingBallStrel3D#dilation(ij.ImageStack)}.
     */
    @Test
    public final void testDilation()
    {
        // create empty image with a single bright pixel
        ImageStack array = ImageStack.create(20, 20, 20, 8);
        array.setVoxel(10, 10, 10, 200.0);

        // create the strel
        Strel3D strel = new SlidingBallStrel3D(5.0);

        // compute closing
        ImageStack result = strel.dilation(array);

        // check values
        assertEquals(result.getBitDepth(), 8);
        assertEquals(200.0, result.getVoxel(10, 10, 10), .01);
        assertEquals(200.0, result.getVoxel( 5, 10, 10), .01);
        assertEquals(200.0, result.getVoxel(15, 10, 10), .01);
        assertEquals(200.0, result.getVoxel(10,  5, 10), .01);
        assertEquals(200.0, result.getVoxel(10, 15, 10), .01);
        assertEquals(200.0, result.getVoxel(10, 10,  5), .01);
        assertEquals(200.0, result.getVoxel(10, 10, 15), .01);
        assertEquals(  0.0, result.getVoxel( 5,  5,  5), .01);
        assertEquals(  0.0, result.getVoxel(15,  5,  5), .01);
        assertEquals(  0.0, result.getVoxel( 5, 15,  5), .01);
        assertEquals(  0.0, result.getVoxel(15, 15,  5), .01);
        assertEquals(  0.0, result.getVoxel( 5,  5, 15), .01);
        assertEquals(  0.0, result.getVoxel(15,  5, 15), .01);
        assertEquals(  0.0, result.getVoxel( 5, 15, 15), .01);
        assertEquals(  0.0, result.getVoxel(15, 15, 15), .01);
    }

    /**
     * Test method for {@link net.ijt.mmorph.strel.SlidingBallStrel3D#erosion(ij.ImageStack)}.
     */
    @Test
    public final void testErosion()
    {
        // create empty image with a single bright pixel
        ImageStack array = ImageStack.create(20, 20, 20, 8);
        Images3D.fill(array, 255.0);
        array.setVoxel(10, 10, 10, 50.0);

        // create the strel
        Strel3D strel = new SlidingBallStrel3D(5.0);

        // compute closing
        ImageStack result = strel.erosion(array);

        // check values
        assertEquals(result.getBitDepth(), 8);
        assertEquals( 50.0, result.getVoxel(10, 10, 10), .01);
        assertEquals( 50.0, result.getVoxel( 5, 10, 10), .01);
        assertEquals( 50.0, result.getVoxel(15, 10, 10), .01);
        assertEquals( 50.0, result.getVoxel(10,  5, 10), .01);
        assertEquals( 50.0, result.getVoxel(10, 15, 10), .01);
        assertEquals( 50.0, result.getVoxel(10, 10,  5), .01);
        assertEquals( 50.0, result.getVoxel(10, 10, 15), .01);
        assertEquals(255.0, result.getVoxel( 5,  5,  5), .01);
        assertEquals(255.0, result.getVoxel(15,  5,  5), .01);
        assertEquals(255.0, result.getVoxel( 5, 15,  5), .01);
        assertEquals(255.0, result.getVoxel(15, 15,  5), .01);
        assertEquals(255.0, result.getVoxel( 5,  5, 15), .01);
        assertEquals(255.0, result.getVoxel(15,  5, 15), .01);
        assertEquals(255.0, result.getVoxel( 5, 15, 15), .01);
        assertEquals(255.0, result.getVoxel(15, 15, 15), .01);
    }

    /**
     * Test method for {@link inra.ijpb.morphology.strel.AbstractStrel3D#closing(ij.ImageStack)}.
     */
    @Test
    public final void testClosing()
    {
        // create empty image with a single bright pixel
        ImageStack array = ImageStack.create(20, 20, 20, 8);
        array.setVoxel(10, 10, 10, 200);

        // create the strel
        Strel3D strel = new SlidingBallStrel3D(5.0);

        // compute closing
        ImageStack result = strel.closing(array);

        // check values
        assertEquals(result.getBitDepth(), 8);
        
        assertEquals(200.0, result.getVoxel(10, 10, 10), .01);
        assertEquals(  0.0, result.getVoxel( 9, 10, 10), .01);
        assertEquals(  0.0, result.getVoxel(11, 10, 10), .01);
        assertEquals(  0.0, result.getVoxel(10,  9, 10), .01);
        assertEquals(  0.0, result.getVoxel(10, 11, 10), .01);
        assertEquals(  0.0, result.getVoxel(10, 10,  9), .01);
        assertEquals(  0.0, result.getVoxel(10, 10, 11), .01);
    }

    /**
     * Test method for {@link inra.ijpb.morphology.strel.AbstractStrel3D#opening(ij.ImageStack)}.
     */
    @Test
    public final void testOpening()
    {
        // create empty image with a single bright pixel
        ImageStack array = ImageStack.create(20, 20, 20, 8);
        Images3D.fill(array, 255.0);
        array.setVoxel(10, 10, 10, 50.0);

        // create the strel
        Strel3D strel = new SlidingBallStrel3D(5.0);

        // compute closing
        ImageStack result = strel.opening(array);

        // check values
        assertEquals(result.getBitDepth(), 8);
        
        assertEquals( 50.0, result.getVoxel(10, 10, 10), .01);
        assertEquals(255.0, result.getVoxel( 9, 10, 10), .01);
        assertEquals(255.0, result.getVoxel(11, 10, 10), .01);
        assertEquals(255.0, result.getVoxel(10,  9, 10), .01);
        assertEquals(255.0, result.getVoxel(10, 11, 10), .01);
        assertEquals(255.0, result.getVoxel(10, 10,  9), .01);
        assertEquals(255.0, result.getVoxel(10, 10, 11), .01);
    }

}
