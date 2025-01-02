package stracescope_code.cam;

import java.awt.image.*;
import java.awt.image.BufferedImage;



public class Frames {
    private static final int FRAME_WIDTH  = 640;
    private static final int FRAME_HEIGHT = 480;

    static public native int   open_shm(String shm_name);
    static public native byte[] get_frame();

    int RGB_pixels[];

    public BufferedImage bi;

    public Frames()
    {
        System.loadLibrary("frames");

        RGB_pixels = new int[FRAME_WIDTH*FRAME_HEIGHT];
    }

    private BufferedImage monochromatic(BufferedImage raw) //zwraca obraz w trybie jednokanałowym
    {
        //return BufferedImage.TYPE_BYTE_BINARY;
        return raw;
    }

    private BufferedImage negative(BufferedImage raw)
    {
        // Dla każdego piksela z wysokości, szerokości wykonać przekształcenie na kolor przeciwny
        for (int y = 0; y < FRAME_HEIGHT; y++) {
            for (int x = 0; x < FRAME_WIDTH; x++) {
                int p = raw.getRGB(x, y); // wartość będąca sumą wszytskich składowych RGB
                int a = (p >> 24) & 0xff; //alpha - bajt 32-24
                int r = (p >> 16) & 0xff; //red - bajty 23-16
                int g = (p >> 8) & 0xff; //green - bajty 15-8
                int b = p & 0xff; //blue - najmłodsze 8 bajtów

                // subtract RGB from 255 -> Otrzymane wartości składowe koloru negatywnego
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;

                // set new RGB value
                p = (a << 24) | (r << 16) | (g << 8) | b; //zestaw wartości rgb do przypisania do obrazu (suma nowych składowych)
                raw.setRGB(x, y, p); //ustawienie koloru na pikselu o współrzędnych x,y i kolorze p
            }
        }
        return raw;
    }

    private BufferedImage add_grid( BufferedImage raw )
    {
        for( int y=0; y<FRAME_HEIGHT; y+=FRAME_HEIGHT/3 ) {
            for ( int x=0; x<FRAME_WIDTH; x+=FRAME_WIDTH/3 )
            {
                //dla każdego piksela znajdującego się na linii siatki o grubości (dać grubość 2-4 px)
            }
        }
        return raw;
    }

    public BufferedImage convert_to_BI(byte buffer[])
    {
        int i, j;

        j = 0;
        for(i = 0; i < RGB_pixels.length; i++)
        {
            RGB_pixels[i] = (int) (buffer[j] << 16) + (buffer[j+1]<< 8) + buffer[j+2];
            j+=3;
        }

        bi = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);

        bi.setRGB(0, 0, FRAME_WIDTH, FRAME_HEIGHT, RGB_pixels, 0, FRAME_WIDTH);

        return bi;
    }
}
