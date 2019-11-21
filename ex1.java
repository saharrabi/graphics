package main;	
	import java.awt.image.BufferedImage;
	import java.io.File;
	import java.io.IOException;

	import javax.imageio.ImageIO;

    
	public class Main {
		public static void main(final String[] args) {
        final String filename = args[0];
        final int col = Integer.parseInt(args[1]);
        final int row = Integer.parseInt(args[2]);
        final int energytype = Integer.parseInt(args[3]);
        final String new_filename = args[4];
        if (!resize(filename, energytype, col, row, new_filename))
            System.out.println("It didn't work");
    }

    public static bool resize(final String filename, final int energy_type, final int col, final int row,
            final String new_filename) {
        final BufferedImage img = read_image(filename);
        int height = img.getHeight();
        int width = img.getWidth();
        int verdel = 0;
        final int hordel = 0;
        final int[][] grey_mat2 = get_grey_scale(rgbMat2, height, width);
        final double[][] energy2 = new double[height][width];
            final double[][] entropy2 = new double[height][width];
            final double[][] forDynamic2 = new double[height][width];         
        // we have these options:
        /*
         * 0)nothing changed 1)width got bigger 1.1)height got bigger 1.2)height got
         * smaller/didn't change 2)width got smaller/didn't change 2.1)height got bigger
         * 2.2)height got smaller/didn't change
         */

        // 0)
        if (width == col && height == row) {
            // put filename into new_filename
            writefile(new_filename, img);
            return 1;
        }
        final int[][][] rgbMat = get_rgb(img, height, width);
        final int[][] grey_mat = get_grey_scale(rgbMat, height, width);
        final double[][] energy = new double[height][width];
        final double[][] entropy = new double[height][width];
        final double[][] forDynamic = new double[height][width];
        Calc_Energy(rgbMat, energy, verdel, hordel);// will need to be fixed when entropy is a thing
        if (energy_type == 1) {
            my_Calc_entropy(grey_mat, entropy, verdel, hordel);
        } else {
            ZEROFY(entropy);
        }
        final int[][][] rgbMat2 = rgbAfterInsertingKSeams_avg(col - width, rgbMat, energy, entropy, forDynamic,
                    energy_type, grey_mat);
        // 1)
        if (col > width && row > height)
            return colRowLonger();
        // 1.2)
        else if (col > width && row <= height)
            return colBiggerRowSmaller();
        // 2)
        else if (col <= width&&row > height) {
            // 2.1)
             return colSmallerRowBigger();
            // 2.2)
        else if (col <= width&&row <= height)
            return colRowSmaller();
        return false;
    }

    // FUNCTIONS

    // ENERGY MATRIX----START
    public static int[][][] get_rgb(final BufferedImage img, final int height, final int width) {
        // copied from last version
        // we will use this function 1 time, which is why it is okay it creates an array
        // with fixed size
        final int[][][] RGBarray = new int[height][width][3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                final int p = img.getRGB(x, y);
                RGBarray[y][x][0] = (p >> 16) & 0xff; /* red */
                RGBarray[y][x][1] = (p >> 8) & 0xff; /* green */
                RGBarray[y][x][2] = p & 0xff;
                /* blue */}
        }
        return RGBarray;
    }

    public static int[][] get_grey_scale(final int[][][] RGBarray, final int height, final int width) {
        final int[][] GREYarray = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                GREYarray[y][x] = (RGBarray[y][x][0] + RGBarray[y][x][1] + RGBarray[y][x][2]) / 3;

            }
        }

        return GREYarray;
    }

    public static void Calc_Energy(final int[][][] rgbMat, final double[][] energyMat, final int Verdel,
            final int Hordel) {
        // puts values of energy type 0 in energyMat
        final int Oheight = rgbMat.length;// original height of picture
        final int Owidth = rgbMat[0].length;// original width of picture
        for (int line = 0; line < Oheight - Hordel; line++) {// for each line we still have in the picture
            for (int column = 0; column < Owidth - Verdel; column++) {// for each column we still have in the picture
                energyMat[line][column] = Calc_Energy_Pixel(rgbMat, line, column, Verdel, Hordel);
            }
        }
    }

    public static double Calc_Energy_Pixel(final int[][][] rgbMat, final int i, final int j, final int Verdel,
            final int Hordel) {
        int sum = 0;
        final int curRed = rgbMat[i][j][0];
        final int curGreen = rgbMat[i][j][1];
        final int curBlue = rgbMat[i][j][2];
        int neigh = 8;
        for (int line = i - 1; line <= i + 1; line++) {// fixed 8/4
            for (int column = j - 1; column <= j + 1; column++) {
                if (line < 0 || column < 0 || line >= rgbMat.length - Hordel || column >= rgbMat[0].length - Verdel) {
                    // if outside of picture
                    neigh--;
                    continue;
                }
                sum += Math.abs(curRed - rgbMat[line][column][0]);
                sum += Math.abs(curGreen - rgbMat[line][column][1]);
                sum += Math.abs(curBlue - rgbMat[line][column][2]);
            }
        }
        return sum / neigh;
    }

    public static void update_energy(final int[][][] rgbMat, final double[][] energy, final int[] seam,
            final int verdel, final int hordel) {
        // note that when energy is given here, the seam we are given as an argument
        // had already been deleted
        for (int i = 0; i < energy.length - hordel; i++) {
            // for each line fix the energy where the seam was before and it's left
            // neighbour
            if (seam[i] != 0) {
                energy[i][seam[i] - 1] = Calc_Energy_Pixel(rgbMat, i, seam[i] - 1, verdel, hordel);
            }
            if (seam[i] != energy[0].length - verdel - 1) {
                energy[i][seam[i]] = Calc_Energy_Pixel(rgbMat, i, seam[i], verdel, hordel);
            }
        }
    }

    public static void update_energy_hor(final int[][][] rgbMat, final double[][] energy, final int[] seam,
            final int verdel, final int hordel) {
        // note seam here is horizontal
        for (int j = 0; j < energy[0].length - verdel; j++) {
            if (seam[j] != 0) {
                energy[seam[j] - 1][j] = Calc_Energy_Pixel(rgbMat, seam[j] - 1, j, verdel, hordel);
            }
            if (seam[j] != energy.length - hordel - 1) {
                energy[seam[j]][j] = Calc_Energy_Pixel(rgbMat, seam[j], j, verdel, hordel);
            }
        }
    }
    // ENERGY MATRIX----END//

    // ENTROPY MY VERSION----START

    public static void my_pmn(final int[][] GREYarray, final int i, final int j, final int Verdel, final int Hordel,
            final double[][] pmnMatrix, final int[][] sums, final int[] firstsum) {
        int sum = 0;
        final int curGrey = GREYarray[i][j];
        if (j == 0) {// if first pixel in line
            for (int line = i - 4; line <= i + 4; line++) {
                for (int column = 0; column <= j + 4; column++) {

                    if (line < 0 || column < 0 || line >= GREYarray.length - Hordel
                            || column >= GREYarray[0].length - Verdel) {
                        // if outside of picture
                        continue;
                    }

                    firstsum[i] += GREYarray[line][j];
                    sum += GREYarray[line][column];
                    sums[i][j] = sum;
                }

            }

            for (int line = 0; line < i - Hordel; line++) {
                if (line < 0 || line >= GREYarray.length - Hordel) {
                    continue;
                }

                firstsum[i] += GREYarray[line][0];
            }
            if (sum == 0) {
                sums[i][j] = sum;
                pmnMatrix[i][j] = 0;
            } else {
                ;
                pmnMatrix[i][j] = curGrey / sums[i][j];

            }
        } else {// not first pixel in line

            if (j - 3 >= 0 && i >= 0) {
                sum = sums[i][j - 3] - firstsum[i];
            }
            for (int line = i - 4; line <= i + 4; line++) {
                if (line < 0 || line >= GREYarray.length - Hordel || j + 3 >= GREYarray[0].length - Verdel) {
                    continue;
                }
                sum += GREYarray[line][j + 3];

            }
            sums[i][j] = sum;
            for (int line = i - 4; line <= i + 4; line++) {
                if (j - 3 < 0 || line < 0 || line >= GREYarray.length - Hordel) {
                    continue;
                }
                firstsum[i] = GREYarray[line][j - 3];
            }
            if (sums[i][j] == 0) {
                pmnMatrix[i][j] = 0;
            } else {

                pmnMatrix[i][j] = curGrey / sums[i][j];
            }
        }
    }

    public static double my_Hi(final double[][] pmnMatrix, final int i, final int j, final int verdel,
            final int hordel) {
        double sum = 0;
        if (j == 0) {// if first pixel in line
            for (int line = i - 4; line <= i + 4; line++) {
                for (int column = j - 4; column <= j + 4; column++) {
                    if (line < 0 || column < 0 || line >= pmnMatrix.length - hordel
                            || column >= pmnMatrix[0].length - verdel) {
                        // if outside of picture
                        continue;
                    }
                    if (pmnMatrix[line][column] == 0) {
                        return 0;
                    } else {
                        sum += pmnMatrix[line][column] * Math.log(pmnMatrix[line][column]);
                    }
                }
            }
            return -sum;
        } else {// not first pixel in line
            sum = pmnMatrix[i][j] * Math.log(pmnMatrix[i][j]);
            for (int line = i - 4; line <= i + 4; line++) {
                if (line < 0 || j - 4 < 0 || line >= pmnMatrix.length - hordel
                        || j + 4 >= pmnMatrix[0].length - verdel) {
                    continue;
                }
                sum += pmnMatrix[line][j + 4] * Math.log(pmnMatrix[line][j + 4]);
                sum -= pmnMatrix[line][j - 4] * Math.log(pmnMatrix[line][j - 4]);
            } // adding/substracting what we need to
            return -sum;
        }
    }

    public static void my_Calc_entropy(final int[][] GREY, final double[][] entropy, final int verdel,
            final int hordel) {
        final double[][] pmn = new double[entropy.length - hordel][entropy[0].length - verdel];
        final int sums[][] = new int[entropy.length - hordel][entropy[0].length - verdel];
        final int firstsum[] = new int[entropy.length - hordel];
        for (int i = 0; i < entropy.length - hordel; i++) {
            for (int j = 0; j < entropy[0].length - verdel; j++) {
                my_pmn(GREY, i, j, verdel, hordel, pmn, sums, firstsum);
                entropy[i][j] = my_Hi(pmn, i, j, verdel, hordel);

            }

        }
    }
    // ENTROPY MY VERSION----END

    // DELETING SEAMS----START
    public static void delete_ver_seam(final int[][] grey_mat, final int energyType, final int[][][] rgbMat,
            final double[][] energy, final double[][] entropy, final int[] seam, final int verdel, final int hordel) {
        // 1)fixing the picture and the energy matrix
        for (int i = 0; i < rgbMat.length - hordel; i++) {// for each real line
            for (int j = seam[i] + 1; j < rgbMat[0].length - verdel; j++) {
                // for each real column that's got to MOVE
                rgbMat[i][j - 1][0] = rgbMat[i][j][0];
                rgbMat[i][j - 1][2] = rgbMat[i][j][2];
                rgbMat[i][j - 1][1] = rgbMat[i][j][1];
                energy[i][j - 1] = energy[i][j];
                if (energyType == 1) {
                    entropy[i][j - 1] = entropy[i][j];
                    grey_mat[i][j - 1] = grey_mat[i][j];
                }
            }
        } // done moving everything to the left
          // Fill the rest in black---START
        for (int i = 0; i < rgbMat.length - hordel; i++) {// for each real line
            rgbMat[i][rgbMat[0].length - verdel - 1][0] = -1;
            rgbMat[i][rgbMat[0].length - verdel - 1][2] = -1;
            rgbMat[i][rgbMat[0].length - verdel - 1][1] = -1;
            energy[i][rgbMat[0].length - verdel - 1] = -1;
            if (energyType == 1) {
                entropy[i][rgbMat[0].length - verdel - 1] = -1;
                grey_mat[i][grey_mat[0].length - verdel - 1] = -1;
            }

        }
        // Fill the rest in black---End
        // now fix the energy
        if (energyType == 0) {
            update_energy(rgbMat, energy, seam, verdel + 1, hordel);// notice we are updating the energy for a different
                                                                    // sized matrix before updating verdel
        } else {
            if (energyType == 1) {
                update_energy(rgbMat, energy, seam, verdel + 1, hordel);
                my_Calc_entropy(grey_mat, entropy, verdel + 1, hordel);
            }
        }
    }

    public static void deleteVerticalSeam(final int[][] grey_mat, final int[][][] rgbMat, final double[][] energy,
            final double[][] entropy, final double[][] forDynamic, final int[] seam_holder, final int energyType,
            final int verdel, final int hordel) {
        if (energyType == 2) {
            forward_Dynamic(energy, entropy, forDynamic, verdel, hordel);
        } else {
            dynamic(energy, entropy, forDynamic, verdel, hordel);
        }
        update_seam(seam_holder, forDynamic, verdel, hordel);
        delete_ver_seam(grey_mat, energyType, rgbMat, energy, entropy, seam_holder, verdel, hordel);
    }

    public static void deleteKVerticalSeams(final int k, final int[][] grey_mat, final int[][][] rgbMat,
            final double[][] energy, final double[][] entropy, final double[][] forDynamic, final int[] seam_holder,
            final int energyType, int verdel, final int hordel) {
        for (int i = 0; i < k; i++) {
            deleteVerticalSeam(grey_mat, rgbMat, energy, entropy, forDynamic, seam_holder, energyType, verdel, hordel);
            verdel = verdel + 1;
        }
    }

    public static void deleteVerticalSeamSTR(final int[][] grey_mat, final int[][][] rgbMat, final double[][] energy,
            final double[][] entropy, final double[][] forDynamic, final int[] seam_holder, final int energyType,
            final int verdel, final int hordel) {
        dynamic(energy, entropy, forDynamic, verdel, hordel);
        update_straight_seam(seam_holder, forDynamic, verdel, hordel);
        delete_ver_seam(grey_mat, energyType, rgbMat, energy, entropy, seam_holder, verdel, hordel);
    }

    public static void deleteKVerticalSeamsSTR(final int k, final int[][] grey_mat, final int[][][] rgbMat,
            final double[][] energy, final double[][] entropy, final double[][] forDynamic, final int[] seam_holder,
            final int energyType, int verdel, final int hordel) {
        for (int i = 0; i < k; i++) {
            deleteVerticalSeamSTR(grey_mat, rgbMat, energy, entropy, forDynamic, seam_holder, energyType, verdel,
                    hordel);
            verdel = verdel + 1;
        }
    }
    // DELETING SEAMS----END

    // DELETING SEAMS HORIZONTAL----START
    public static void delete_hor_seam(final int[][] grey_mat, final int energyType, final int[][][] rgbMat,
            final double[][] energy, final double[][] entropy, final int[] seam, final int verdel, final int hordel) {
        // 1)fixing the picture and the energy matrix
        for (int j = 0; j < rgbMat[0].length - verdel; j++) {// for each real column
            for (int i = seam[j] + 1; i < rgbMat.length - hordel; i++) {
                // for each real line that has to MOVE up
                rgbMat[i - 1][j][0] = rgbMat[i][j][0];
                rgbMat[i - 1][j][1] = rgbMat[i][j][1];
                rgbMat[i - 1][j][2] = rgbMat[i][j][2];
                energy[i - 1][j] = energy[i][j];
                if (energyType == 1) {
                    entropy[i - 1][j] = entropy[i][j];
                    grey_mat[i - 1][j] = grey_mat[i][j];
                }
            }
        } // done moving everything up
          // Fill the rest in black
        for (int j = 0; j < rgbMat[0].length - verdel; j++) {
            rgbMat[rgbMat.length - hordel - 1][j][0] = -1;
            rgbMat[rgbMat.length - hordel - 1][j][2] = -1;
            rgbMat[rgbMat.length - hordel - 1][j][1] = -1;
            energy[rgbMat.length - hordel - 1][j] = -1;
            if (energyType == 1) {
                entropy[rgbMat.length - hordel - 1][j] = -1;
                grey_mat[rgbMat.length - hordel - 1][j] = -1;
            }
        }
        // fix the energy
        if (energyType == 0) {
            update_energy_hor(rgbMat, energy, seam, verdel, hordel + 1);
        } else {
            if (energyType == 1) {
                update_energy_hor(rgbMat, energy, seam, verdel, hordel + 1);
                my_Calc_entropy(grey_mat, entropy, verdel, hordel + 1);
            }
        }
    }

    public static void deleteHorizontalSeam(final int[][] grey_mat, final int[][][] rgbMat, final double[][] energy,
            final double[][] entropy, final double[][] forDynamic, final int[] seam_holder, final int energyType,
            final int verdel, final int hordel) {
        if (energyType == 2) {
            forward_Dynamic_hor(energy, entropy, forDynamic, verdel, hordel);
        } else {
            dynamic_hor(energy, entropy, forDynamic, verdel, hordel);
        }
        update_seam_hor(seam_holder, forDynamic, verdel, hordel);
        delete_hor_seam(grey_mat, energyType, rgbMat, energy, entropy, seam_holder, verdel, hordel);
    }

    public static void deleteKHorizontalSeams(final int k, final int[][] grey_mat, final int[][][] rgbMat,
            final double[][] energy, final double[][] entropy, final double[][] forDynamic, final int[] seam_holder,
            final int energyType, final int verdel, int hordel) {
        for (int i = 0; i < k; i++) {
            deleteHorizontalSeam(grey_mat, rgbMat, energy, entropy, forDynamic, seam_holder, energyType, verdel,
                    hordel);
            hordel = hordel + 1;
        }
    }

    public static void deleteHorizontalSeamSTR(final int[][] grey_mat, final int[][][] rgbMat, final double[][] energy,
            final double[][] entropy, final double[][] forDynamic, final int[] seam_holder, final int energyType,
            final int verdel, final int hordel) {
        dynamic_hor(energy, entropy, forDynamic, verdel, hordel);
        update_straight_seam_hor(seam_holder, forDynamic, verdel, hordel);
        delete_hor_seam(grey_mat, energyType, rgbMat, energy, entropy, seam_holder, verdel, hordel);
    }

    public static void deleteKHorizontalSeamsSTR(final int k, final int[][] grey_mat, final int[][][] rgbMat,
            final double[][] energy, final double[][] entropy, final double[][] forDynamic, final int[] seam_holder,
            final int energyType, final int verdel, int hordel) {
        for (int i = 0; i < k; i++) {
            deleteHorizontalSeamSTR(grey_mat, rgbMat, energy, entropy, forDynamic, seam_holder, energyType, verdel,
                    hordel);
            hordel = hordel + 1;
        }
    }
    // DELETING SEAMS HORIZONTAL----END

    // INSERT SEAMS----START
    public static int[][] get_seams(final int k, final int[][][] rgbMatCOPY, final double[][] energyCOPY,
            final double[][] entropyCOPY, final double[][] forDynamicCOPY, final int energyType,
            final int[][] grey_matCOPY) {
        // k=how many seams we want to find
        final int[][] seams = new int[k][rgbMatCOPY.length];
        for (int i = 0; i < k; i++) {
            if (energyType == 2) {
                forward_Dynamic(energyCOPY, entropyCOPY, forDynamicCOPY, 0, 0);
            } else {
                dynamic(energyCOPY, entropyCOPY, forDynamicCOPY, 0, 0);
            }
            update_seam(seams[i], forDynamicCOPY, 0, 0);
            ampliphy_ver_seam(energyType, rgbMatCOPY, grey_matCOPY, energyCOPY, entropyCOPY, seams[i], 0, 0);
        }
        return seams;
    }

    public static void ampliphy_ver_seam(final int energyType, final int[][][] rgbMat, final int[][] grey_mat,
            final double[][] energy, final double[][] entropy, final int[] seam, final int verdel, final int hordel) {
        // 1)fixing the picture and the energy matrix

        for (int i = 0; i < rgbMat.length - hordel; i++) {// for each real line
            rgbMat[i][seam[i]][0] += 10;
            rgbMat[i][seam[i]][1] += 10;
            rgbMat[i][seam[i]][2] += 10;
            if (energyType == 1) {
                grey_mat[i][seam[i]] += 10;
            }
        }
        // now fix the energy(for now it works only for energy type 0.
        update_ampliphied_energy(energyType, rgbMat, energy, seam, verdel, hordel);
        if (energyType == 1) {
            my_Calc_entropy(grey_mat, entropy, verdel, hordel);
        }
    }

    public static void update_ampliphied_energy(final int energytype, final int[][][] rgbMat, final double[][] energy,
            final int[] seam, final int verdel, final int hordel) {
        for (int i = 0; i < energy.length - hordel; i++) {
            if (seam[i] != 0) {
                energy[i][seam[i] - 1] = Calc_Energy_Pixel(rgbMat, i, seam[i] - 1, verdel, hordel);
            }
            energy[i][seam[i]] = Calc_Energy_Pixel(rgbMat, i, seam[i], verdel, hordel);
            if (seam[i] != energy[0].length - verdel - 1) {
                energy[i][seam[i] + 1] = Calc_Energy_Pixel(rgbMat, i, seam[i] + 1, verdel, hordel);
            }
        }
    }

    public static int[][][] newPicRGB(final int[][] seams, final int NewWidth, final int[][][] rgbMat) {
        final int[][][] result = new int[rgbMat.length][NewWidth][3];// maybe the col+1 is not necessary
        // put rgb values into new matrix
        for (int i = 0; i < rgbMat.length; i++) {
            for (int j = 0; j < rgbMat[0].length; j++) {
                result[i][j][0] = rgbMat[i][j][0];
                result[i][j][1] = rgbMat[i][j][1];
                result[i][j][2] = rgbMat[i][j][2];
            }
        } // after this rgbMat should be inside result
          // insert the seam duplicates into the new matrix
        for (int k = 0; k < seams.length; k++) {
            for (int line = 0; line < seams[0].length; line++) {

                for (int j = NewWidth - 1; j >= seams[k][line] + 1; j--) {// maybe +2
                    result[line][j][0] = result[line][j - 1][0];
                    result[line][j][1] = result[line][j - 1][1];
                    result[line][j][2] = result[line][j - 1][2];
                }
                result[line][seams[k][line] + 1][0] = result[line][seams[k][line]][0];
                result[line][seams[k][line] + 1][1] = result[line][seams[k][line]][1];
                result[line][seams[k][line] + 1][2] = result[line][seams[k][line]][2];
            }
        }
        return result;
    }

    public static int[][][] rgbAfterInsertingKSeams(final int k, final int[][][] rgbMat, final double[][] energy,
            final double[][] entropy, final double[][] forDynamic, final int energyType, final int[][] grey_mat) {
        final int[][][] rgbMatCOPY = copy3D(rgbMat);
        final int[][] grey_matCOPY = copy2D_int(grey_mat);
        final double[][] energyCOPY = copy2D_double(energy);
        final double[][] entropyCOPY = copy2D_double(entropy);
        final double[][] forDynamicCOPY = copy2D_double(forDynamic);

        final int[][] seams = get_seams(k, rgbMatCOPY, energyCOPY, entropyCOPY, forDynamicCOPY, energyType,
                grey_matCOPY);
        final int[][][] result = newPicRGB(seams, rgbMat[0].length + k, rgbMat);
        return result;
    }

    public static int[][][] newPicRGB_avg(final int[][] seams, final int NewWidth, final int[][][] rgbMat) {
        final int[][][] result = new int[rgbMat.length][NewWidth][3];// maybe the col+1 is not necessary
        // put rgb values into new matrix
        for (int i = 0; i < rgbMat.length; i++) {
            for (int j = 0; j < rgbMat[0].length; j++) {
                result[i][j][0] = rgbMat[i][j][0];
                result[i][j][1] = rgbMat[i][j][1];
                result[i][j][2] = rgbMat[i][j][2];
            }
        } // after this rgbMat should be inside result
          // insert the seam duplicates into the new matrix
        for (int k = 0; k < seams.length; k++) {
            for (int line = 0; line < seams[0].length; line++) {

                for (int j = NewWidth - 1; j >= seams[k][line] + 1; j--) {// maybe +2
                    result[line][j][0] = result[line][j - 1][0];
                    result[line][j][1] = result[line][j - 1][1];
                    result[line][j][2] = result[line][j - 1][2];
                }
                // averaging by placement of seam (left/middle/left)
                if (seams[k][line] - 1 >= 0 && seams[k][line] + 2 <= seams[0].length) {
                    // middle
                    result[line][seams[k][line] + 1][0] = (result[line][seams[k][line]][0]
                            + result[line][seams[k][line] - 1][0] + result[line][seams[k][line] + 2][0]) / 3;
                    result[line][seams[k][line] + 1][1] = (result[line][seams[k][line]][1]
                            + result[line][seams[k][line] - 1][1] + result[line][seams[k][line] + 2][1]) / 3;
                    result[line][seams[k][line] + 1][2] = (result[line][seams[k][line]][2]
                            + result[line][seams[k][line] - 1][2] + result[line][seams[k][line] + 2][2]) / 3;
                } else {
                    if (seams[k][line] - 1 < 0) {
                        // left
                        result[line][seams[k][line] + 1][0] = (result[line][seams[k][line]][0]
                                + result[line][seams[k][line] + 2][0]) / 2;
                        result[line][seams[k][line] + 1][1] = (result[line][seams[k][line]][1]
                                + result[line][seams[k][line] + 2][1]) / 2;
                        result[line][seams[k][line] + 1][2] = (result[line][seams[k][line]][2]
                                + result[line][seams[k][line] + 2][2]) / 2;
                    } else {
                        // right
                        result[line][seams[k][line] + 1][0] = (result[line][seams[k][line]][0]
                                + result[line][seams[k][line] - 1][0]) / 2;
                        result[line][seams[k][line] + 1][1] = (result[line][seams[k][line]][1]
                                + result[line][seams[k][line] - 1][1]) / 2;
                        result[line][seams[k][line] + 1][2] = (result[line][seams[k][line]][2]
                                + result[line][seams[k][line] - 1][2]) / 2;
                    }
                }
            }
        }
        return result;
    }

    public static int[][][] rgbAfterInsertingKSeams_avg(final int k, final int[][][] rgbMat, final double[][] energy,
            final double[][] entropy, final double[][] forDynamic, final int energyType, final int[][] grey_mat) {
        final int[][][] rgbMatCOPY = copy3D(rgbMat);
        final int[][] grey_matCOPY = copy2D_int(grey_mat);
        final double[][] energyCOPY = copy2D_double(energy);
        final double[][] entropyCOPY = copy2D_double(entropy);
        final double[][] forDynamicCOPY = copy2D_double(forDynamic);

        final int[][] seams = get_seams(k, rgbMatCOPY, energyCOPY, entropyCOPY, forDynamicCOPY, energyType,
                grey_matCOPY);
        return newPicRGB_avg(seams, rgbMat[0].length + k, rgbMat);
    }
    // INSERT SEAMS----END

    // INSERT SEAMS HORIZONTAL----START
    public static int[][] get_hor_seams(final int k, final int[][][] rgbMatCOPY, final double[][] energyCOPY,
            final double[][] entropyCOPY, final double[][] forDynamicCOPY, final int energyType,
            final int[][] grey_matCOPY) {
        final int[][] seams = new int[k][rgbMatCOPY[0].length];
        for (int j = 0; j < k; j++) {
            if (energyType == 2) {
                forward_Dynamic_hor(energyCOPY, entropyCOPY, forDynamicCOPY, 0, 0);
            } else {
                dynamic_hor(energyCOPY, entropyCOPY, forDynamicCOPY, 0, 0);
            }
            update_seam_hor(seams[j], forDynamicCOPY, 0, 0);
            ampliphy_hor_seam(energyType, rgbMatCOPY, grey_matCOPY, energyCOPY, entropyCOPY, seams[j], 0, 0);
        }
        return seams;
    }

    public static void ampliphy_hor_seam(final int energyType, final int[][][] rgbMat, final int[][] grey_mat,
            final double[][] energy, final double[][] entropy, final int[] seam, final int verdel, final int hordel) {
        // fixing the picture and the energy matrix

        for (int j = 0; j < rgbMat[0].length - verdel; j++) {// for each real column
            rgbMat[seam[j]][j][0] += 10;
            rgbMat[seam[j]][j][1] += 10;
            rgbMat[seam[j]][j][2] += 10;
            if (energyType == 1) {
                grey_mat[seam[j]][j] += 10;
            }
        }
        // now fix the energy(for now it works only for energy type 0.
        update_ampliphied_energy_hor(energyType, rgbMat, energy, seam, verdel, hordel);
        if (energyType == 1) {
            my_Calc_entropy(grey_mat, entropy, verdel, hordel);
        }
    }

    public static void update_ampliphied_energy_hor(final int energytype, final int[][][] rgbMat,
            final double[][] energy, final int[] seam, final int verdel, final int hordel) {
        for (int j = 0; j < energy[0].length - verdel; j++) {
            if (seam[j] != 0) {
                energy[seam[j] - 1][j] = Calc_Energy_Pixel(rgbMat, seam[j] - 1, j, verdel, hordel);
            }
            energy[seam[j]][j] = Calc_Energy_Pixel(rgbMat, seam[j], j, verdel, hordel);
            if (seam[j] != energy.length - hordel - 1) {
                energy[seam[j] + 1][j] = Calc_Energy_Pixel(rgbMat, seam[j] + 1, j, verdel, hordel);
            }
        }
    }

    public static int[][][] newPicRGB_hor(final int[][] seams, final int NewHeight, final int[][][] rgbMat) {
        final int[][][] result = new int[NewHeight][rgbMat[0].length][3];// maybe the col+1 is not necessary
        // put rgb values into new matrix
        for (int i = 0; i < rgbMat.length; i++) {
            for (int j = 0; j < rgbMat[0].length; j++) {
                result[i][j][0] = rgbMat[i][j][0];
                result[i][j][1] = rgbMat[i][j][1];
                result[i][j][2] = rgbMat[i][j][2];
            }
        } // after this rgbMat should be inside result
          // insert the seam duplicates into the new matrix
        for (int k = 0; k < seams.length; k++) {
            for (int col = 0; col < seams[0].length; col++) {

                for (int i = NewHeight - 1; i >= seams[k][col] + 1; i--) {
                    result[i][col][0] = result[i - 1][col][0];
                    result[i][col][1] = result[i - 1][col][1];
                    result[i][col][2] = result[i - 1][col][2];
                }
                result[seams[k][col] + 1][col][0] = result[seams[k][col]][col][0];
                result[seams[k][col] + 1][col][1] = result[seams[k][col]][col][1];
                result[seams[k][col] + 1][col][2] = result[seams[k][col]][col][2];
            }
        }
        return result;
    }

    public static int[][][] rgbAfterInsertingKSeams_hor(final int k, final int[][][] rgbMat, final double[][] energy,
            final double[][] entropy, final double[][] forDynamic, final int energyType, final int[][] grey_mat) {
        final int[][][] rgbMatCOPY = copy3D(rgbMat);
        final int[][] grey_matCOPY = copy2D_int(grey_mat);
        final double[][] energyCOPY = copy2D_double(energy);
        final double[][] entropyCOPY = copy2D_double(entropy);
        final double[][] forDynamicCOPY = copy2D_double(forDynamic);
        final int[][] seams = get_hor_seams(k, rgbMatCOPY, energyCOPY, entropyCOPY, forDynamicCOPY, energyType,
                grey_matCOPY);
        return newPicRGB_hor(seams, rgbMat.length + k, rgbMat);
    }

    public static int[][][] newPicRGB_avg_hor(final int[][] seams, final int NewHeight, final int[][][] rgbMat) {
        final int[][][] result = new int[NewHeight][rgbMat[0].length][3];// maybe the col+1 is not necessary
        // put rgb values into new matrix
        for (int i = 0; i < rgbMat.length; i++) {
            for (int j = 0; j < rgbMat[0].length; j++) {
                result[i][j][0] = rgbMat[i][j][0];
                result[i][j][1] = rgbMat[i][j][1];
                result[i][j][2] = rgbMat[i][j][2];
            }
        } // after this rgbMat should be inside result
          // insert the seam duplicates into the new matrix
        for (int k = 0; k < seams.length; k++) {
            for (int col = 0; col < seams[0].length; col++) {

                for (int i = NewHeight - 1; i >= seams[k][col] + 1; i--) {
                    result[i][col][0] = result[i - 1][col][0];
                    result[i][col][1] = result[i - 1][col][1];
                    result[i][col][2] = result[i - 1][col][2];
                }
                // averaging by placement of seam (left/middle/left)
                if (seams[k][col] - 1 >= 0 && seams[k][col] + 2 <= seams[0].length) {
                    // middle
                    result[seams[k][col] + 1][col][0] = (result[seams[k][col]][col][0]
                            + result[seams[k][col] - 1][col][0] + result[seams[k][col] + 2][col][0]) / 3;
                    result[seams[k][col] + 1][col][1] = (result[seams[k][col]][col][1]
                            + result[seams[k][col] - 1][col][1] + result[seams[k][col] + 2][col][1]) / 3;
                    result[seams[k][col] + 1][col][2] = (result[seams[k][col]][col][2]
                            + result[seams[k][col] - 1][col][2] + result[seams[k][col] + 2][col][2]) / 3;
                } else {
                    if (seams[k][col] - 1 < 0) {
                        // left
                        result[seams[k][col] + 1][col][0] = (result[seams[k][col]][col][0]
                                + result[seams[k][col] + 2][col][0]) / 2;
                        result[seams[k][col] + 1][col][1] = (result[seams[k][col]][col][1]
                                + result[seams[k][col] + 2][col][1]) / 2;
                        result[seams[k][col] + 1][col][2] = (result[seams[k][col]][col][2]
                                + result[seams[k][col] + 2][col][2]) / 2;
                    } else {
                        // right
                        result[seams[k][col] + 1][col][0] = (result[seams[k][col]][col][0]
                                + result[seams[k][col] - 1][col][0]) / 2;
                        result[seams[k][col] + 1][col][1] = (result[seams[k][col]][col][1]
                                + result[seams[k][col] - 1][col][1]) / 2;
                        result[seams[k][col] + 1][col][2] = (result[seams[k][col]][col][2]
                                + result[seams[k][col] - 1][col][2]) / 2;
                    }
                }
            }
        }
        return result;
    }

    public static int[][][] rgbAfterInsertingKSeams_avg_hor(final int k, final int[][][] rgbMat,
            final double[][] energy, final double[][] entropy, final double[][] forDynamic, final int energyType,
            final int[][] grey_mat) {
        final int[][][] rgbMatCOPY = copy3D(rgbMat);
        final int[][] grey_matCOPY = copy2D_int(grey_mat);
        final double[][] energyCOPY = copy2D_double(energy);
        final double[][] entropyCOPY = copy2D_double(entropy);
        final double[][] forDynamicCOPY = copy2D_double(forDynamic);
        final int[][] seams = get_hor_seams(k, rgbMatCOPY, energyCOPY, entropyCOPY, forDynamicCOPY, energyType,
                grey_matCOPY);
        return newPicRGB_avg_hor(seams, rgbMat.length + k, rgbMat);
    }
    // INSERT SEAMS HORIZONTAL----END

    // MATRIX COPYING----START
    public static int[][][] copy3D(final int[][][] matrix) {
        final int[][][] copy = new int[matrix.length][matrix[0].length][matrix[0][0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                for (int r = 0; r < matrix[0][0].length; r++) {
                    copy[i][j][r] = matrix[i][j][r];
                }
            }
        }
        return copy;
    }

    public static double[][] copy2D_double(final double[][] matrix) {
        final double[][] copy = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                copy[i][j] = matrix[i][j];
            }
        }
        return copy;
    }

    public static int[][] copy2D_int(final int[][] matrix) {
        final int[][] copy = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                copy[i][j] = matrix[i][j];
            }
        }
        return copy;
    }

    public static void ZEROFY(final double[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = 0;
            }
        }
    }
    // MATRIX COPYING----END

    // DYNAMIC----START
    public static void dynamic(final double[][] energy, final double[][] entropy, final double[][] copy,
            final int verdel, final int hordel) {
        /*
         * 1)implement the dynamic programming code 2)from the place you reached in the
         * lowest row, begin going straight up
         */
        // FIRST LINE START
        int i = 1;
        for (int j = 0; j < energy[0].length - verdel; j++) {
            /*
             * if we are in the middle do1 else: if we are in the left do: else we are in
             * the right
             */
            if (j - 1 > 0 && j + 1 < energy[0].length - verdel) {
                // middle
                copy[i][j] = (energy[i][j] + entropy[i][j]) + Math.min(
                        Math.min(energy[i - 1][j - 1] + entropy[i - 1][j - 1], energy[i - 1][j] + entropy[i - 1][j]),
                        energy[i - 1][j + 1] + entropy[i - 1][j + 1]);
            } else {
                if (j - 1 < 0) {
                    // left
                    copy[i][j] = (energy[i][j] + entropy[i][j]) + Math.min(energy[i - 1][j] + entropy[i - 1][j],
                            energy[i - 1][j + 1] + entropy[i - 1][j + 1]);
                } else {
                    // right
                    copy[i][j] = (energy[i][j] + entropy[i][j]) + Math.min(energy[i - 1][j] + entropy[i - 1][j],
                            energy[i - 1][j - 1] + entropy[i - 1][j - 1]);
                }
            }
        }
        // FIRST LINE END
        for (i = 2; i < energy.length - hordel; i++) {
            for (int j = 0; j < energy[0].length - verdel; j++) {
                /*
                 * if we are in the middle do1 else: if we are in the left do: else we are in
                 * the right
                 */
                // for now lets assume we are at the third line
                if (j - 1 > 0 && j + 1 < energy[0].length - verdel) {
                    // middle
                    copy[i][j] = (energy[i][j] + entropy[i][j]) + Math.min(
                            Math.min(copy[i - 1][j - 1] + entropy[i - 1][j - 1], copy[i - 1][j] + entropy[i - 1][j]),
                            copy[i - 1][j + 1] + entropy[i - 1][j + 1]);
                } else {
                    if (j - 1 < 0) {
                        // left
                        copy[i][j] = (energy[i][j] + entropy[i][j]) + Math.min(copy[i - 1][j] + entropy[i - 1][j],
                                copy[i - 1][j + 1] + entropy[i - 1][j + 1]);
                    } else {
                        // right
                        copy[i][j] = (energy[i][j] + entropy[i][j]) + Math.min(copy[i - 1][j] + entropy[i - 1][j],
                                copy[i - 1][j - 1] + entropy[i - 1][j - 1]);
                    }
                }
            }
        }
    }

    public static void update_seam(final int[] ver_seam_holder, final double[][] dynamicEnergy, final int verdel,
            final int hordel) {
        // note we are going to put look only at the places we still care about
        final int last_line_index = ver_seam_holder.length - hordel - 1;
        /*
         * 1)find minimum at last_line_index line 2) go up by comparing values
         */
        int min_index = 0;
        double min = dynamicEnergy[last_line_index][0];
        for (int j = 1; j < dynamicEnergy[0].length - verdel; j++) {
            if (dynamicEnergy[last_line_index][j] < min) {
                min = dynamicEnergy[last_line_index][j];
                min_index = j;
            }
        } // done finding the minimum in the last line
        ver_seam_holder[last_line_index] = min_index;
        for (int i = last_line_index - 1; i >= 0; i--) {// going UP
            /*
             * if we are in the middle do1 else: if we are in the left do: else we are in
             * the right
             */
            if (min_index > 0 && min_index < dynamicEnergy[0].length - verdel - 1) {
                // middle
                if (dynamicEnergy[i][min_index - 1] < dynamicEnergy[i][min_index]) {
                    if (dynamicEnergy[i][min_index + 1] < dynamicEnergy[i][min_index - 1]) {
                        // place j+1 is minimal
                        ver_seam_holder[i] = min_index + 1;
                        min_index = min_index + 1;
                    } else {
                        // place j-1 is minimal
                        ver_seam_holder[i] = min_index - 1;
                        min_index = min_index - 1;
                    }
                } else {
                    if (dynamicEnergy[i][min_index] < dynamicEnergy[i][min_index + 1]) {
                        // place j is minimal
                        ver_seam_holder[i] = min_index;
                    } else {
                        // place j+1 is minimal
                        ver_seam_holder[i] = min_index + 1;
                        min_index = min_index + 1;
                    }
                }
            } else {
                if (min_index == 0) {
                    // left
                    if (dynamicEnergy[i][min_index] < dynamicEnergy[i][min_index + 1]) {
                        // j is minimal
                        ver_seam_holder[i] = min_index;
                    } else {
                        // j+1 is minimal
                        ver_seam_holder[i] = min_index + 1;
                        min_index = min_index + 1;
                    }
                } else {
                    // right
                    if (dynamicEnergy[i][min_index] < dynamicEnergy[i][min_index - 1]) {
                        // j is minimal
                        ver_seam_holder[i] = min_index;
                    } else {
                        // j-1 is minimal
                        ver_seam_holder[i] = min_index - 1;
                        min_index = min_index - 1;
                    }
                }
            }
        }
    }

    public static void update_straight_seam(final int[] ver_seam_holder, final double[][] dynamicEnergy,
            final int verdel, final int hordel) {
        // note we are going to put look only at the places we still care about
        final int last_line_index = ver_seam_holder.length - hordel - 1;
        /*
         * 1)find minimum at last_line_index line 2) go up by comparing values
         */
        int min_index = 0;
        double min = dynamicEnergy[last_line_index][0];
        for (int j = 1; j < dynamicEnergy[0].length - verdel; j++) {
            if (dynamicEnergy[last_line_index][j] < min) {
                min = dynamicEnergy[last_line_index][j];
                min_index = j;
            }
        } // done finding the minimum in the last line
        ver_seam_holder[last_line_index] = min_index;
        for (int i = last_line_index - 1; i >= 0; i--) {
            ver_seam_holder[i] = min_index;
        }
    }
    // DYNAMIC----END

    // DYNAMIC_HOR----START
    public static void dynamic_hor(final double[][] energy, final double[][] entropy, final double[][] forDynamic,
            final int verdel, final int hordel) {
        /*
         * 1)implement the dynamic programming code 2)from the place you reached in the
         * rightmost column, begin going left
         */
        // FIRST COLUMN START
        int j = 1;
        for (int i = 0; i < energy.length - hordel; i++) {
            /*
             * if we are in the middle do1 else: if we are in the left do: else we are in
             * the right
             */
            if (i - 1 > 0 && i + 1 < energy.length - hordel) {
                // middle
                forDynamic[i][j] = (energy[i][j] + entropy[i][j]) + Math.min(
                        Math.min(energy[i - 1][j - 1] + entropy[i - 1][j - 1], energy[i][j - 1] + entropy[i][j - 1]),
                        energy[i + 1][j - 1] + entropy[i + 1][j - 1]);
            } else {
                if (i - 1 < 0) {
                    // left
                    forDynamic[i][j] = (energy[i][j] + entropy[i][j]) + Math.min(energy[i][j - 1] + entropy[i][j - 1],
                            energy[i + 1][j - 1] + entropy[i + 1][j - 1]);
                } else {
                    // right
                    forDynamic[i][j] = (energy[i][j] + entropy[i][j]) + Math.min(energy[i][j - 1] + entropy[i][j - 1],
                            energy[i - 1][j - 1] + entropy[i - 1][j - 1]);
                }
            }
        }
        // FIRST LINE END
        for (j = 2; j < energy[0].length - verdel; j++) {
            for (int i = 0; i < energy.length - hordel; i++) {
                /*
                 * if we are in the middle do1 else: if we are in the left do: else we are in
                 * the right
                 */
                if (i - 1 > 0 && i + 1 < energy.length - hordel) {
                    // middle
                    forDynamic[i][j] = (energy[i][j] + entropy[i][j]) + Math.min(
                            Math.min(forDynamic[i - 1][j - 1] + entropy[i - 1][j - 1],
                                    forDynamic[i][j - 1] + entropy[i][j - 1]),
                            forDynamic[i + 1][j - 1] + entropy[i + 1][j - 1]);
                } else {
                    if (i - 1 < 0) {
                        // left
                        forDynamic[i][j] = (energy[i][j] + entropy[i][j])
                                + Math.min(forDynamic[i][j - 1] + entropy[i][j - 1],
                                        forDynamic[i + 1][j - 1] + entropy[i + 1][j - 1]);
                    } else {
                        // right
                        forDynamic[i][j] = (energy[i][j] + entropy[i][j])
                                + Math.min(forDynamic[i][j - 1] + entropy[i][j - 1],
                                        forDynamic[i - 1][j - 1] + entropy[i - 1][j - 1]);
                    }
                }
            }
        }
    }

    public static void update_seam_hor(final int[] hor_seam_holder, final double[][] dynamicEnergy, final int verdel,
            final int hordel) {
        final int last_column_index = hor_seam_holder.length - verdel - 1;
        /*
         * 1)find minimum at last_column_index column 2) go left by comparing values
         */
        int min_index = 0;
        double min = dynamicEnergy[0][last_column_index];
        for (int i = 1; i < dynamicEnergy.length - hordel; i++) {
            if (dynamicEnergy[i][last_column_index] < min) {
                min = dynamicEnergy[i][last_column_index];
                min_index = i;
            }
        } // done finding the minimum in the last column
        hor_seam_holder[last_column_index] = min_index;
        for (int j = last_column_index - 1; j >= 0; j--) {// going to the left
            /*
             * if we are in the middle do1 else: if we are in the left do: else we are in
             * the right
             */
            if (min_index > 0 && min_index < dynamicEnergy.length - hordel - 1) {
                // middle
                if (dynamicEnergy[min_index - 1][j] < dynamicEnergy[min_index][j]) {
                    if (dynamicEnergy[min_index + 1][j] < dynamicEnergy[min_index - 1][j]) {
                        // place i+1 is minimal
                        hor_seam_holder[j] = min_index + 1;
                        min_index = min_index + 1;
                    } else {
                        // place j-1 is minimal
                        hor_seam_holder[j] = min_index - 1;
                        min_index = min_index - 1;
                    }
                } else {
                    if (dynamicEnergy[min_index][j] < dynamicEnergy[min_index + 1][j]) {
                        // place i is minimal
                        hor_seam_holder[j] = min_index;
                    } else {
                        // place i+1 is minimal
                        hor_seam_holder[j] = min_index + 1;
                        min_index = min_index + 1;
                    }
                }
            } else {
                if (min_index == 0) {
                    // left
                    if (dynamicEnergy[min_index][j] < dynamicEnergy[min_index + 1][j]) {
                        // i is minimal
                        hor_seam_holder[j] = min_index;
                    } else {
                        // j+1 is minimal
                        hor_seam_holder[j] = min_index + 1;
                        min_index = min_index + 1;
                    }
                } else {
                    // right
                    if (dynamicEnergy[min_index][j] < dynamicEnergy[min_index - 1][j]) {
                        // j is minimal
                        hor_seam_holder[j] = min_index;
                    } else {
                        // j-1 is minimal
                        hor_seam_holder[j] = min_index - 1;
                        min_index = min_index - 1;
                    }
                }
            }
        }
    }

    public static void update_straight_seam_hor(final int[] hor_seam_holder, final double[][] dynamicEnergy,
            final int verdel, final int hordel) {
        final int last_column_index = hor_seam_holder.length - verdel - 1;
        /*
         * 1)find minimum at last_column_index column 2) go left by comparing values
         */
        int min_index = 0;
        double min = dynamicEnergy[0][last_column_index];
        for (int i = 1; i < dynamicEnergy.length - hordel; i++) {
            if (dynamicEnergy[i][last_column_index] < min) {
                min = dynamicEnergy[i][last_column_index];
                min_index = i;
            }
        } // done finding the minimum in the last column
        hor_seam_holder[last_column_index] = min_index;
        for (int j = last_column_index - 1; j >= 0; j--) {
            hor_seam_holder[j] = min_index;
        }
    }
    // DYNAMIC_HOR----END

    // IMAGE HANDLING----START
    public static BufferedImage read_image(final String filename) {
        BufferedImage img = null;
        File f = null;
        try {
            f = new File(filename);
            img = ImageIO.read(f);
        } catch (final IOException e) {
            System.out.println(e);
        }
        return img;
    }

    public static void writefile(final String new_file_path, final BufferedImage img) {
        try {
            final File f = new File(new_file_path);
            ImageIO.write(img, "jpg", f);
        } catch (final IOException e) {
            System.out.println(e);
        }
    }
    // IMAGE HANDLING----END

    // FORWARD DYNAMIC---START
    public static void forward_Dynamic(final double[][] energy, final double[][] entropy, final double[][] forDynamic,
            final int verdel, final int hordel) {
        /*
         * (a)Cl(i,j)=Math.Abs()+Math.Abs(); (b)Cu(i,j)=Math.Abs();
         * (c)Cr(i,j)=Math.Abs()+Math.Abs();
         */
        // FIRST LINE START
        int i = 1;
        double opLeft = 9000, opMiddle = 9000, opRight = 9000;
        for (int j = 0; j < energy[0].length - verdel; j++) {
            if (j - 1 > 0 && j + 1 < energy[0].length - verdel - 1) {
                opMiddle = Math.abs((energy[i][j + 1] + entropy[i][j + 1]) - (energy[i][j - 1] + entropy[i][j - 1]));
            }
            if (j - 1 < 0) {
                // we are in the left, calc only right seam option
                opLeft = energy[i - 1][j + 1] + opMiddle
                        + Math.abs((energy[i - 1][j] + entropy[i - 1][j]) - (energy[i][j + 1] + entropy[i][j + 1]));
            }
            if (j + 1 > energy[0].length - verdel - 1) {
                // we are in the right, calc only left seam option
                opRight = energy[i - 1][j - 1] + opMiddle
                        + Math.abs((energy[i - 1][j] + entropy[i - 1][j]) - (energy[i][j - 1] + entropy[i][j - 1]));

            }
            opMiddle += energy[i - 1][j];
            forDynamic[i][j] = Math.min(opMiddle, Math.min(opRight, opLeft));
        }
        // FIRST LINE END
        for (i = 2; i < energy.length - hordel; i++) {
            for (int j = 0; j < energy[0].length - verdel; j++) {
                if (j - 1 > 0 && j + 1 < energy[0].length - verdel - 1) {
                    opMiddle = Math
                            .abs((energy[i][j + 1] + entropy[i][j + 1]) - (energy[i][j - 1] + entropy[i][j - 1]));
                }
                if (j - 1 < 0) {
                    // we are in the left, calc only right seam option
                    opLeft = forDynamic[i - 1][j + 1] + opMiddle
                            + Math.abs((forDynamic[i - 1][j]) - (energy[i][j + 1] + entropy[i][j + 1]));
                }
                if (j + 1 > energy[0].length - verdel - 1) {
                    // we are in the right, calc only left seam option
                    opRight = forDynamic[i - 1][j - 1] + opMiddle
                            + Math.abs((forDynamic[i - 1][j]) - (energy[i][j - 1] + entropy[i][j - 1]));
                }
                opMiddle += forDynamic[i - 1][j];
                forDynamic[i][j] = Math.min(opMiddle, Math.min(opRight, opLeft));
            }
        }
    }

    public static void forward_Dynamic_hor(final double[][] energy, final double[][] entropy,
            final double[][] forDynamic, final int verdel, final int hordel) {
        /*
         * (a)Cl(i,j)=Math.Abs()+Math.Abs(); (b)Cu(i,j)=Math.Abs();
         * (c)Cr(i,j)=Math.Abs()+Math.Abs();
         */
        // FIRST LINE START
        int j = 1;
        double opLeft = 9000, opMiddle = 9000, opRight = 9000;
        for (int i = 0; i < energy.length - hordel; i++) {
            if (i - 1 > 0 && i + 1 < energy.length - hordel - 1) {
                opMiddle = Math.abs((energy[i + 1][j] + entropy[i + 1][j]) - (energy[i - 1][j] + entropy[i - 1][j]));
            }
            if (i - 1 < 0) {
                // we are in the left
                opLeft = energy[i + 1][j - 1] + opMiddle
                        + Math.abs((energy[i][j - 1] + entropy[i][j - 1]) - (energy[i + 1][j] + entropy[i + 1][j]));
            }
            if (i + 1 > energy.length - hordel - 1) {
                // we are in the right
                opRight = energy[i - 1][j - 1] + opMiddle
                        + Math.abs((energy[i][j - 1] + entropy[i][j - 1]) - (energy[i - 1][j] + entropy[i - 1][j]));
            }
            opMiddle += energy[i][j - 1];
            forDynamic[i][j] = Math.min(opMiddle, Math.min(opRight, opLeft));
        }
        // FIRST LINE END
        for (j = 2; j < energy[0].length - verdel; j++) {
            for (int i = 0; i < energy.length - hordel; i++) {
                // middle shouldn't be checked
                if (i - 1 > 0 && i + 1 < energy.length - hordel - 1) {
                    opMiddle = Math
                            .abs((energy[i + 1][j] + entropy[i + 1][j]) - (energy[i - 1][j] + entropy[i - 1][j]));
                }
                if (i - 1 < 0) {
                    // we are in the left, calc only right seam option
                    opLeft = forDynamic[i + 1][j - 1] + opMiddle
                            + Math.abs((forDynamic[i][j - 1]) - (energy[i + 1][j] + entropy[i + 1][j]));

                }
                if (i + 1 > energy.length - hordel - 1) {
                    // we are in the right, calc only left seam option
                    opRight = energy[i - 1][j - 1] + opMiddle
                            + Math.abs((energy[i - 1][j] + entropy[i - 1][j]) - forDynamic[i][j - 1]);
                }
                opMiddle += forDynamic[i][j - 1];
                forDynamic[i][j] = Math.min(opMiddle, Math.min(opRight, opLeft));
            }
        }
    }
    // FORWARD DYNAMIC---END

    public static boolen colRowBigger() {
        {
            // add k columns
            // int[][][] rgbMat2=rgbAfterInsertingKSeams(col-width, rgbMat, energy, entropy,
            // forDynamic, energy_type, grey_mat);
            width = col;
            // add k rows
        
            // int[][][] rgbMat3=rgbAfterInsertingKSeams_hor(row-height, rgbMat2, energy2,
            // entropy2,forDynamic2, energy_type, grey_mat2);
            final int[][][] rgbMat3 = rgbAfterInsertingKSeams_avg_hor(row - height, rgbMat2, energy2, entropy2,
                    forDynamic2, energy_type, grey_mat2);
            height = row;

            final BufferedImage imgRESULT = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int p = imgRESULT.getRGB(x, y);
                    final int a = (p >> 24) & 0xff;
                    p = (a << 24) | (rgbMat3[y][x][0] << 16) | (rgbMat3[y][x][1] << 8) | rgbMat3[y][x][2];
                    imgRESULT.setRGB(x, y, p);
                }
            }
            writefile(new_filename, imgRESULT);
            return true;
        }
    }

    public static boolean colBiggerRowSma() {
        // add k columns
        // int[][][] rgbMat2=rgbAfterInsertingKSeams(col-width, rgbMat, energy, entropy,
        // forDynamic, energy_type, grey_mat);
        final int[][][] rgbMat2 = rgbAfterInsertingKSeams_avg(col - width, rgbMat, energy, entropy, forDynamic,
                energy_type, grey_mat);
        width = col;
        // remove k rows
        final int[] seam_holder = new int[width];
        deleteKHorizontalSeams(height - row, grey_mat2, rgbMat2, energy2, entropy2, forDynamic2, seam_holder,
                energy_type, verdel, hordel);
        // deleteKHorizontalSeamsSTR(height-row, grey_mat2, rgbMat2, energy2, entropy2,
        // forDynamic2, seam_holder, energy_type, verdel, hordel);
        height = row;
        final BufferedImage imgRESULT = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = imgRESULT.getRGB(x, y);
                final int a = (p >> 24) & 0xff;
            p = (a << 24) | (rgbMat2[y][x][0] << 16) | (rgbMat2[y][x][1] << 8) | rgbMat2[y][x][2];
            imgRESULT.setRGB(x, y, p);
        }
    }
    writefile(new_filename, imgRESULT);
    return true;
}
public static boolean colSmallerRowBi(){
    {
        // add k rows
        // int[][][] rgbMat2=rgbAfterInsertingKSeams_hor(row-height, rgbMat, energy,
        // entropy, forDynamic, energy_type, grey_mat);
        final int[][][] rgbMat2 = rgbAfterInsertingKSeams_avg_hor(row - height, rgbMat, energy, entropy,
                forDynamic, energy_type, grey_mat);
        height = row;
        // remove k columns
        final int[] seam_holder = new int[height];
        deleteKVerticalSeams(width - col, grey_mat2, rgbMat2, energy2, entropy2, forDynamic2, seam_holder,
                energy_type, verdel, hordel);
        // deleteKVerticalSeamsSTR(width-col, grey_mat2, rgbMat2, energy2, entropy2,
        // forDynamic2, seam_holder, energy_type, verdel, hordel);
        width = col;

        final BufferedImage imgRESULT = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = imgRESULT.getRGB(x, y);
                final int a = (p >> 24) & 0xff;
                p = (a << 24) | (rgbMat2[y][x][0] << 16) | (rgbMat2[y][x][1] << 8) | rgbMat2[y][x][2];
                imgRESULT.setRGB(x, y, p);
            }
        }
        writefile(new_filename, imgRESULT);
        return true;
    }
}
public static boolean colRowSmaller(){
    {
        // remove k columns--don't forget to update verdel now
        final int[] seam_holder = new int[height];
        deleteKVerticalSeams(width - col, grey_mat, rgbMat, energy, entropy, forDynamic, seam_holder,
                energy_type, verdel, hordel);
        // deleteKVerticalSeamsSTR(width-col, grey_mat, rgbMat, energy, entropy,
        // forDynamic, seam_holder, energy_type, verdel, hordel);
        verdel = width - col;
        // remove k rows
        final int[] seam_holder2 = new int[width];
        deleteKHorizontalSeams(height - row, grey_mat, rgbMat, energy, entropy, forDynamic, seam_holder2,
                energy_type, verdel, hordel);
        // deleteKHorizontalSeamsSTR(height-row, grey_mat, rgbMat, energy, entropy,
        // forDynamic, seam_holder2, energy_type, verdel, hordel);
        width = col;
        height = row;

        final BufferedImage imgRESULT = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p = imgRESULT.getRGB(x, y);
                final int a = (p >> 24) & 0xff;
                p = (a << 24) | (rgbMat[y][x][0] << 16) | (rgbMat[y][x][1] << 8) | rgbMat[y][x][2];
                imgRESULT.setRGB(x, y, p);
            }
        }
        writefile(new_filename, imgRESULT);
        return true;
    }
}
}
