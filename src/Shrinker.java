import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

public class Shrinker {
    private static boolean verbose = true;

    public static void main(String[] args){
        if(args.length < 3){
            System.out.println("Usage:\tShrinker filepath width height [outfilename]");
            System.exit(1);
        }

        String filename = "", filepath = "", filedir = "";
        try {
            filepath = args[0];
            int dirdelim_bs = filepath.lastIndexOf("\\") + 1, dirdelim_fs = filepath.lastIndexOf("/") + 1;
            int dirdelim = Math.max(dirdelim_bs, dirdelim_fs);

            if(dirdelim != 0) {
                filename = filepath.substring(dirdelim);
                filedir = filepath.substring(0, dirdelim);
            }else{
                filename = filepath;
                filedir = "./";
            }
        }catch(ArrayIndexOutOfBoundsException aioobe){
            System.out.println("Specify a filepath");
            System.out.println("Usage:\tShrinker filepath width height [outfilename]");
            System.exit(1);
        }
        if(verbose)
            System.out.println("Filepath: " + filepath + " Filename: " + filename + " Filedir: " + filedir);

        File file = new File(filepath);
        BufferedImage img = null;
        try{
            img = ImageIO.read(file);
        }catch(IOException ioe){
            System.out.println("Couldn't parse image: " + file.getAbsolutePath());
            System.out.println("Usage:\tShrinker filepath width height [outfilename]");
            System.exit(1);
        }
        if(verbose)
            System.out.println("Image dimensions: " + img.getWidth() + "x" + img.getHeight());

        int width = img.getWidth(), height = img.getHeight();
        try{
            width = Integer.parseInt(args[1]);
            height = Integer.parseInt(args[2]);
            if(width <= 0 || height <= 0 || width > img.getWidth() || height > img.getHeight()){
                throw new NumberFormatException();
            }
        }catch(NumberFormatException nfe){
            System.out.println("Invalid width or height parameter");
            System.out.println("Usage:\tShrinker filepath width height [outfilename]");
            System.exit(1);
        }

        if(verbose)
            System.out.println("Lodef shrinking image to " + width + "x" + height);
        long t = System.currentTimeMillis();
        img = lodef_shrink(img, width, height);
        int del = (int)(System.currentTimeMillis() - t);

        if(verbose)
            System.out.println("Saving file");
        String outfilename = filename;
        String outfilepath = filepath;
        File outfile = file;
        String ext = filename.substring(filename.lastIndexOf(".") + 1);
        if(args.length > 3){
            outfilename = args[3] + "." + ext;
            System.out.println("Write to " + outfile.getAbsolutePath());
            outfile = new File(filedir + outfilename);
            System.out.println("Write to " + outfile.getAbsolutePath());
        }
        try{
            if(verbose)
                System.out.println("Writing shrunk image to: " + outfile.getCanonicalPath());
            ImageIO.write(img, ext, outfile);
        }catch(IOException ioe){
            System.out.println("Couldn't write image to: " + outfile.getAbsolutePath());
            System.out.println("Usage:\tShrinker filepath width height [outfilename]");
            System.exit(1);
        }

        System.out.println("Shrunk image " + filename + " to " + width + "x" + height + " and saved to " + outfilename);
        System.out.println("Done in " + del/1000.0 + "s");
    }

    private static int[][] edgedetect(int[][] pxl){
        System.out.println(pxl[0].length + "x" + pxl.length);
        int width = pxl[0].length, height = pxl.length;

        // vertical
        int[][] ky = new int[height][width];
        for(int j = 1; j < height-1; j++){
            ky[j][0] =  pxl[j-1][0] + 0*pxl[j-1][0] - pxl[j-1][1]
                            + 2*pxl[j][0] + 0*pxl[j][0] - 2*pxl[j][1]
                          + pxl[j+1][0] + 0*pxl[j+1][0] - pxl[j+1][1];
            ky[j][width-1] =  pxl[j-1][width-2] +  0*pxl[j-1][width-1] - pxl[j-1][width-1]
                                  + 2*pxl[j][width-2] + 0*pxl[j][width-1] - 2*pxl[j][width-1]
                                + pxl[j+1][width-2] + 0*pxl[j+1][width-1] - pxl[j+1][width-1];
        }
        for(int i = 1; i < width-1; i++){
            ky[0][i] = + pxl[0][i-1] - 0*pxl[0][i] - pxl[0][i+1]
                         + 2*pxl[0][i-1] + 0*pxl[0][i] - 2*pxl[0][i+1]
                          + pxl[1][i-1] - 0*pxl[1][i] - pxl[1][i+1];
            ky[height-1][i] = + pxl[height-2][i-1] - 0*pxl[height-2][i] - pxl[height-2][i+1]
                                + 2*pxl[height-1][i-1] + 0*pxl[height-1][i] - 2*pxl[height-1][i+1]
                                 + pxl[height-1][i-1] - 0*pxl[height-1][i] - pxl[height-1][i+1];
        }
        for(int j = 1; j < height-1; j++){
            for(int i = 1; i < width-1; i++){
                ky[j][i] = + pxl[j-1][i-1] - 0*pxl[j-1][i] - pxl[j-1][i+1]
                                + 2*pxl[j][i-1] + 0*pxl[j][i] - 2*pxl[j][i+1]
                              + pxl[j+1][i-1] - 0*pxl[j+1][i] - pxl[j+1][i+1];
            }
        }

        // horizontal
        int[][] kx = new int[height][width];
        for(int j = 1; j < height-1; j++){
            kx[j][0] = - pxl[j-1][0] - 2*pxl[j-1][0] - pxl[j-1][1]
                  //  - pxl[j][0] + 8*pxl[j][0] - pxl[j][1]
                    + pxl[j+1][0] + 2*pxl[j+1][0] + pxl[j+1][1];
            kx[j][width-1] = - pxl[j-1][width-2] - 2*pxl[j-1][width-1] - pxl[j-1][width-1]
                  //  - pxl[j][width-2] + 8*pxl[j][width-1] - pxl[j][width-1]
                    + pxl[j+1][width-2] + 2*pxl[j+1][width-1] + pxl[j+1][width-1];
        }
        for(int i = 1; i < width-1; i++){
            kx[0][i] = - pxl[0][i-1] - 2*pxl[0][i] - pxl[0][i+1]
                  //  - pxl[0][i-1] + 8*pxl[0][i] - pxl[0][i+1]
                    - pxl[1][i-1] - 2*pxl[1][i] - pxl[1][i+1];
            kx[height-1][i] = - pxl[height-2][i-1] - 2*pxl[height-2][i] - pxl[height-2][i+1]
                  //  - pxl[height-1][i-1] + 8*pxl[height-1][i] - pxl[height-1][i+1]
                    + pxl[height-1][i-1] + 2*pxl[height-1][i] + pxl[height-1][i+1];
        }
        for(int j = 1; j < height-1; j++){
            for(int i = 1; i < width-1; i++){
                kx[j][i] = - pxl[j-1][i-1] - 2*pxl[j-1][i] - pxl[j-1][i+1]
                     //   - pxl[j][i-1] + 8*pxl[j][i] - pxl[j][i+1]
                        + pxl[j+1][i-1] + 2*pxl[j+1][i] + pxl[j+1][i+1];
            }
        }

        int[][] edges = new int[height][width];
        for(int j = 0; j < height; j++){
            for(int i = 0; i < width; i++){
                edges[j][i] = (int)Math.sqrt(kx[j][i]*kx[j][i] + ky[j][i]*ky[j][i]);
            }
        }
        return edges;
    }

    private static int[][] rmv_min_row(int[][] pxl){
        int[][] energy = edgedetect(pxl);
        int row_energy, min_row = -1, min_row_energy = Integer.MAX_VALUE;
        for(int j = 0; j < energy.length; j++){
            row_energy = 0;
            for(int i = 0; i < energy[0].length; i++){
                row_energy += energy[j][i];
            }
            if(min_row_energy > row_energy){
                min_row_energy = row_energy;
                min_row = j;
            }
        }
        int[][] newpxl = new int[pxl.length-1][pxl[0].length];
        int newj = 0;
        for(int j = 0; j < pxl.length; j++){
            if(j == min_row)
                continue;
            newpxl[newj] = pxl[j];
            newj++;
        }
        return newpxl;
    }

    private static int[][] rmv_min_col(int[][] pxl){
        int[][] energy = edgedetect(pxl);
        int col_energy, min_col = -1, min_col_energy = Integer.MAX_VALUE;
        for(int i = 0; i < energy[0].length; i++){
            col_energy = 0;
            for(int j = 0; j < energy.length; j++){
                col_energy += energy[j][i];
            }
            if(min_col_energy > col_energy){
                min_col_energy = col_energy;
                min_col = i;
            }
        }
        int[][] newpxl = new int[pxl.length][pxl[0].length-1];
        int newi = 0;
        for(int i = 0; i < pxl[0].length; i++){
            if(i == min_col)
                continue;
            for(int j = 0; j < pxl.length; j++){
                newpxl[j][newi] = pxl[j][i];
            }
            newi++;
        }
        return newpxl;
    }

    private static BufferedImage lodef_shrink(BufferedImage img, int w, int h){

        int rmvcols = img.getWidth()-w;
        int rmvrows = img.getHeight()-h;

        int[][] pxl = new int[img.getHeight()][img.getWidth()];
        for(int j = 0; j < img.getHeight(); j++){
            for(int i = 0; i < img.getWidth(); i++){
                Color rgb = new Color(img.getRGB(i, j));
                pxl[j][i] = (rgb.getRed() + rgb.getGreen() + rgb.getBlue())/3;
            }
        }

        if(verbose)
            System.out.println("Removing " + rmvrows + " rows and " + rmvcols + " columns");
        for(int i = 0; i < 2*Math.min(rmvcols,rmvrows); i++){
            if((i & 1) == 1){
                pxl = rmv_min_row(pxl);
            }else{
                pxl = rmv_min_col(pxl);
            }
        }

        int diff = rmvrows - rmvcols;
        if(diff > 0){
            for(int j = 0; j < diff; j++){
                pxl = rmv_min_row(pxl);
            }
        }else if(diff < 0){
            for(int i = 0; i < -diff; i++){
                pxl = rmv_min_col(pxl);
            }
        }

        BufferedImage newimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for(int j = 0; j < h; j++){
            for(int i = 0; i < w; i++){
                int c = Math.min(Math.max(pxl[j][i], 0), 255);
                newimg.setRGB(i, j, new Color(c, c, c).getRGB());
            }
        }
        if(verbose)
            System.out.println("Made new image");
        return newimg;
    }

    private static BufferedImage hidef_shrink(BufferedImage img, int w, int h){
        return img;
    }
}
