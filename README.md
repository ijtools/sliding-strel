# Sliding Strel plugin

Implementation of mathematical morphology based filtering using sliding structuring elements,
provided as plugins for the [ImageJ](http://imagej.net/Welcome) software.
The main purpose of this plugin is to compare running time of morphological operations (dilation, erosion, closing...)
using different implementations of disk or ball structuring elements. 

The development is based on the
[MorphoLibJ](https://github.com/ijpb/MorphoLibJ) library, for comparing with other implementations, and for 3D image management utilities.

## Installation and usage

To use the plugin within ImageJ, simply add the jar file into the "plugins" directory 
of the ImageJ/Fiji installation.

You need to restart ImageJ, or use the "refresh menus" command. After that, two new entries appear in the 
"Plugins -> MorphoLibJ Plus -> Sliding Strel" menu:

* **Sliding Disk Filtering** allows to perform morphological filtering on 2D grayscale (or binary) images
* **Sliding Disk Filtering 3D** allows to perform morphological filtering on 3D grayscale (or binary) images

Each plugin opens a dialog that allows to choose the type of operation (dilation, erosion, closing...), 
the algorithm to use (Sliding, Naive, or ImageJ native), the size of the structuring element, and an option 
to display the structuring element.

The "Sliding" version of the structuring element is faster than the naive one. The native ImageJ version uses 
multi-threading, resulting in faster running time for "small" structuring elements (usually less than 50 pixels or 
voxels, depending on hardware).

## Known bugs or limitations 

The definition of the radius is not the same for the native ImageJ implementation as for the two other one.
This results in slight differences between resulting images.

Sliding structuring elements are not multi-threaded, while this is technically feasible.
