/*-
 * #%L
 * Mathematical morphology library and plugins for ImageJ/Fiji.
 * %%
 * Copyright (C) 2014 - 2017 INRA.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package net.ijt.mmorph;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import inra.ijpb.algo.DefaultAlgoListener;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Morphology.Operation;
import inra.ijpb.morphology.Strel3D;
import inra.ijpb.util.IJUtils;
import net.ijt.mmorph.strel.NaiveBallStrel3D;
import net.ijt.mmorph.strel.SlidingBallStrel3D;


/**
 * Morphological filtering of 3D grayscale images using several implementation
 * variants of ball structuring elements. Possible implementations are:
 * <ul>
 * <li>Naive implementation considering all the neighbors of the current
 * voxel</li>
 * <li>Sliding ball implementation that considers only changes between
 * successive positions of the structuring element</li>
 * <li>ImageJ's native implementation, that also considers all the neighbors of
 * the current voxel</li>
 * </ul>
 * 
 * @author David Legland
 *
 */

public class SlidingBallFilter3DPlugin implements PlugIn 
{
    // the list of available algorithms for comparison
    public final static String[] algoList = new String[] {"Sliding Ball", "Naive Ball", "ImageJ native"};

    // Settings for initializing the plugin dialog
    Operation op = Operation.DILATION;
    int algoIndex = 0;
    double radius = 2;
    boolean showStrel;
    
    
    /* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public void run(String arg) 
	{
		if ( IJ.getVersion().compareTo("1.48a") < 0 )
		{
			IJ.error( "Morphological Filter 3D", "ERROR: detected ImageJ version " + IJ.getVersion()  
					+ ".\nThis plugin requires version 1.48a or superior, please update ImageJ!" );
			return;
		}
		
		ImagePlus imagePlus = WindowManager.getCurrentImage();
		if (imagePlus == null) 
		{
			IJ.error("No image", "Need at least one image to work");
			return;
		}
		
		// create the dialog
		GenericDialog gd = new GenericDialog("Morphological Filters (3D)");
		
		gd.addChoice("Operation", Operation.getAllLabels(), 
				this.op.toString());
		gd.addChoice("Method", algoList, algoList[algoIndex]);
        gd.addNumericField("Radius (in voxels)", 2, 0);
		gd.addCheckbox("Show Element", false);
		
		// Could also add an option for the type of operation
		gd.showDialog();
		
		if (gd.wasCanceled())
			return;
		
		long t0 = System.currentTimeMillis();

		// extract chosen parameters
		op = Operation.fromLabel(gd.getNextChoice());
		algoIndex = gd.getNextChoiceIndex();
		radius = gd.getNextNumber();		
		showStrel = gd.getNextBoolean();
		
		// Create structuring element of the given size
        // Create structuring element with the chosen radius
        Strel3D strel;
        switch (algoIndex)
        {
            case 0: strel = new SlidingBallStrel3D(radius); break;
            case 1: strel = new NaiveBallStrel3D(radius); break;
            case 2: strel = Strel3D.Shape.BALL.fromRadius((int) radius); break;

            default:
                throw new RuntimeException("Unkown structuring element type");
        }

		strel.showProgress(true);
		DefaultAlgoListener.monitor(strel);
		
		// Eventually display the structuring element used for processing 
		if (showStrel)
		{
			showStrelImage(strel);
		}
		
		// Execute core of the plugin
		ImagePlus resPlus = process(imagePlus, op, strel);

		if (resPlus == null)
			return;

		// Display the result image
		resPlus.show();
		resPlus.setSlice(imagePlus.getCurrentSlice());

		// Display elapsed time
		long t1 = System.currentTimeMillis();
		IJUtils.showElapsedTime(op.toString(), t1 - t0, imagePlus);
	}


	/**
	 * Displays the current structuring element in a new ImagePlus. 
	 * @param strel the 3D structuring element to display
	 */
	private void showStrelImage(Strel3D strel) 
	{
		// Size of the strel image (little bit larger than strel)
		int[] dim = strel.getSize();
		int sizeX = dim[0] + 10; 
		int sizeY = dim[1] + 10;
		int sizeZ = dim[2] + 10;
		
		// Creates strel image by dilating a point
		ImageStack stack = ImageStack.create(sizeX, sizeY, sizeZ, 8);
		stack.setVoxel(sizeX / 2, sizeY / 2, sizeZ / 2, 255);
		stack = Morphology.dilation(stack, strel);
		
		// Display strel image
		ImagePlus strelImage = new ImagePlus("Structuring Element", stack);
		strelImage.setSlice(((sizeZ - 1) / 2) + 1);
		strelImage.show();
	}

	public ImagePlus process(ImagePlus image, Operation op, Strel3D strel) 
	{
		// Check validity of parameters
		if (image == null)
			return null;
		
		// extract the input stack
		ImageStack inputStack = image.getStack();

		// apply morphological operation
		ImageStack resultStack = op.apply(inputStack, strel);

		// create the new image plus from the processor
		String newName = image.getShortTitle() + "-" + op.toString();
		ImagePlus resultPlus = new ImagePlus(newName, resultStack);
		resultPlus.copyScale(image);
		
		// return the created array
		return resultPlus;
	}
}
