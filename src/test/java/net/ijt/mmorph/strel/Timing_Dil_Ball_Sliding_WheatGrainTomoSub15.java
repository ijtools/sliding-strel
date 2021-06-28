/**
 * 
 */
package net.ijt.mmorph.strel;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import inra.ijpb.morphology.Strel3D;
import net.sci.table.Table;
import net.sci.table.io.DelimitedTableWriter;
import net.sci.table.io.TableWriter;

/**
 * @author dlegland
 *
 */
public class Timing_Dil_Ball_Sliding_WheatGrainTomoSub15
{

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException
    {
        String fileName = Timing_Dil_Ball_Sliding_WheatGrainTomoSub15.class.getResource("/images/wheatGrain_tomo_180a_sub15.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        
        assertNotNull(imagePlus);

        ImageStack image = imagePlus.getStack();
        
        System.out.println("image size: " + image.getWidth() + " x " + image.getHeight() + " x " + image.getSize());
        
        double[] radiusList= new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 10.0, 12.0, 14.0, 16.0, 20.0, 30.0, 50.0};
        int nRadius = radiusList.length;
        int nRepets = 10;
        
        Table table = Table.create(nRepets, nRadius + 1);
        String[] colNames = new String[nRadius+1];
        colNames[0] = "repet";
        for (int iRadius = 0; iRadius < nRadius; iRadius++)
        {
            colNames[iRadius+1] = String.format(Locale.ENGLISH, "r=%.0f", radiusList[iRadius]);
        }
        table.setColumnNames(colNames);

        for (int iRepet = 0; iRepet < nRepets; iRepet++)
        {
            System.out.println(String.format(Locale.ENGLISH, "repet %d / %d", iRepet, nRepets));
            table.setValue(iRepet,  0, iRepet + 1);
            
            for (int iRadius = 0; iRadius < radiusList.length; iRadius++)
            {
                double radius = radiusList[iRadius];
                Strel3D strel = new SlidingBallStrel3D(radius);
                
                long t0 = System.nanoTime();
                strel.dilation(image);
                long t1 = System.nanoTime();
                // convert to time step in ms
                double dt = (t1 - t0) / 1_000_000_000.0;
                table.setValue(iRepet, iRadius + 1, dt);
                System.out.println(String.format(Locale.ENGLISH, "radius = %5.1f, time = %7.3f s", radius, dt));

                TableWriter writer = new DelimitedTableWriter(";");
                writer.writeTable(table, new File("timing_dil_Ball_sliding_WheatGrainTomoSub15.csv"));
            }
        }
        
//        // compute average
//        int[] inds = new int[nRadius];
//        for (int i = 0; i < nRadius; i++)
//        {
//            inds[i] = i + 1;
//        }
//        Table table2 = Table.selectColumns(table, inds);
        
    }
}
