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
import ij.Prefs;
import ij.process.ImageProcessor;
import inra.ijpb.morphology.Strel;
import net.sci.table.Table;
import net.sci.table.io.DelimitedTableWriter;
import net.sci.table.io.TableWriter;

/**
 * @author dlegland
 *
 */
public class ImageJDiskStrelTiming_MaizeTomoSlice
{

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException
    {
        String fileName = ImageJDiskStrelTiming_MaizeTomoSlice.class.getResource("/images/wheatGrain_tomo_180a_z630.tif").getFile();
        ImagePlus imagePlus = IJ.openImage(fileName);
        
        assertNotNull(imagePlus);

        ImageProcessor image = imagePlus.getProcessor();
        
        System.out.println("image size: " + image.getWidth() + " x " + image.getHeight());
        
        double[] radiusList= new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 15.0, 20.0, 25.0, 30.0, 40.0, 50.0, 70.0, 100.0, 125.0, 150.00, 200.0};
        int nRadius = radiusList.length;
        int nRepets = 10;
        
        // use a single thread for comparisons
        Prefs.setThreads(1);
        
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
                Strel strel = Strel.Shape.DISK.fromRadius((int) radius);
                
                long t0 = System.nanoTime();
                strel.dilation(image);
                long t1 = System.nanoTime();
                // convert to time step in ms
                double dt = (t1 - t0) / 1_000_000.0;
                table.setValue(iRepet, iRadius + 1, dt);
                System.out.println(String.format(Locale.ENGLISH, "radius = %5.1f, time = %7.2f ms", radius, dt));
            }
        }
        
        TableWriter writer = new DelimitedTableWriter(";");
        
        writer.writeTable(table, new File("timing_imagejDisk_WheatGrainSlice.csv"));
 
    }
}
