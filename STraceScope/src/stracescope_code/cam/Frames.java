package stracescope_code.cam;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.Buffer;
import java.awt.Color;


public class Frames {
    private static final int FRAME_WIDTH  = 640;
    private static final int FRAME_HEIGHT = 480;

    // monochromatic, negative
    static private boolean[] editing_settings = {false, false, false, false};
    static private double brightness = 0.5;
    static private double contrast = 0.5;
    static private int y_px = (int)FRAME_HEIGHT/2;
    static private int x_px = (int)FRAME_WIDTH/2;
    static private double scale = 1.0;

    static public native int   open_shm(String shm_name);
    static public native byte[] get_frame();

    static int RGB_pixels[];

    public static BufferedImage bi;

    static public void set_scale(double new_s)
    {
        scale = new_s;
    }

    static public void set_brightness(double new_b)
    {
        brightness = new_b;
    }

    static public void set_contrast(double new_c)
    {
        contrast = new_c;
    }

    static public int get_x_px()
    {
        return y_px;
    }

    static public int get_y_px()
    {
        return x_px;
    }

    static public void update_edit_settings(boolean[] new_settings)
    {
        editing_settings = new_settings;
        System.out.println("Updated editing settings: " + editing_settings[0] +", " + editing_settings[1] +", " + editing_settings[2]);
    }

    static private BufferedImage scale_BI(BufferedImage raw)
    {
        //obliczenie nowego wymiaru
        
    }

    static public BufferedImage edit_BI(BufferedImage raw)
    {
        //BufferedImage edited = raw;
        BufferedImage edited = new BufferedImage(raw.getWidth(), raw.getHeight(), raw.getType());
        for (int x = 0; x < raw.getWidth(); x++) {
            for (int y = 0; y < raw.getHeight(); y++) {
                edited.setRGB(x, y, raw.getRGB(x, y));
            }
        }

        if( editing_settings[3] ) return raw;

        //One-channel
        if( editing_settings[0] )  edited = monochromatic(edited);

        //Negative
        if( editing_settings[1] ) edited = negative(edited);

        // Add grid
        if( editing_settings[2] ) edited = add_grid(edited);
        edited = change_brighness(edited);
        edited = change_contrast(edited);

        return edited;
    }

    public Frames()
    {
        System.loadLibrary("frames");
        RGB_pixels = new int[FRAME_WIDTH*FRAME_HEIGHT];
    }

    static public BufferedImage monochromatic(BufferedImage raw) //zwraca obraz w trybie jednokanałowym
    {
        BufferedImage one_channel =  new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);

        // Iterowanie po każdym pikselu
        for (int y = 0; y < FRAME_HEIGHT; y++) {
            for (int x = 0; x < FRAME_WIDTH; x++) {
                // Pobieramy wartość RGB piksela
                int rgb = raw.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                //uniwersalny wzór na przekształcenie w odcienie szarości
                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);

                int grayRgb = (gray << 16) | (gray << 8) | gray;
                one_channel.setRGB(x, y, grayRgb);
            }
        }

        return one_channel;
    }

    static public BufferedImage negative(BufferedImage raw)
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

    static private BufferedImage add_grid( BufferedImage raw )
    {
        BufferedImage edited = raw;
        int white = (255 << 24) | (255 << 16) | (255 << 8) | 255;
        for( int y=FRAME_HEIGHT/3; y<FRAME_HEIGHT; y+=FRAME_HEIGHT/3 )
        {
            for(int x=0; x<FRAME_WIDTH; x++)
            {
                for(int i=0; i<6; i++)
                {
                    if(x+i < FRAME_WIDTH && y < FRAME_HEIGHT) edited.setRGB(x + i, y, white);
                }
            }
        }

        for ( int x=FRAME_WIDTH/3; x<FRAME_WIDTH; x+=FRAME_WIDTH/3 )
        {
            for(int y=0; y<FRAME_HEIGHT; y++)
            {
                for(int i=0; i<6; i++)
                {
                    if(x < FRAME_WIDTH && y+i < FRAME_HEIGHT) edited.setRGB(x, y+i, white);
                }
            }
        }

        return edited;
    }

    static public BufferedImage convert_to_BI(byte buffer[])
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

    static public BufferedImage load_from_file(String path)
    {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(path));
            System.out.println("image loaded ok");
        } catch (IOException e) {}

        return img;
    }

    static public byte[] convert_BI_to_bytes( BufferedImage BI )
    {
        int width = BI.getWidth();
        int height = BI.getHeight();
        byte[] buffer = new byte[width * height * 3]; // 3, bo RGB

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = BI.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                int index = (y * width + x) * 3;
                buffer[index] = (byte) r;
                buffer[index + 1] = (byte) g;
                buffer[index + 2] = (byte) b;
            }
        }

        return buffer;
    }

    public static BufferedImage change_brighness(BufferedImage image) {
        if (brightness < 0 || brightness > 1) {
            throw new IllegalArgumentException("Brightness factor must be between 0 and 1");
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // Tworzymy nowy obraz do przechowywania wyniku
        BufferedImage adjustedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);

                // Rozdzielamy na składowe R, G, B
                int a = (argb >> 24) & 0xFF; // Kanał alfa (przezroczystość)
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                // Modyfikujemy jasność każdej składowej
                r = clamp((int) (r * (brightness / 0.5)), 0, 255);
                g = clamp((int) (g * (brightness / 0.5)), 0, 255);
                b = clamp((int) (b * (brightness / 0.5)), 0, 255);

                // Łączymy zmodyfikowane składowe
                int newArgb = (a << 24) | (r << 16) | (g << 8) | b;
                adjustedImage.setRGB(x, y, newArgb);
            }
        }

        return adjustedImage;
    }

    public static BufferedImage change_contrast(BufferedImage img) {
        // Przekształcamy zakres kontrastu na -1 do +1, gdzie 0 oznacza brak zmian
        double contrastFactor = 2 * (contrast - 0.5);

        // Tworzymy nowy obraz o tych samych wymiarach
        BufferedImage output = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                // Pobieramy piksel
                int argb = img.getRGB(x, y);
                Color color = new Color(argb, true);

                // Rozdzielamy komponenty koloru
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();

                // Zmieniamy kontrast dla każdej składowej
                r = adjustChannel(r, contrastFactor);
                g = adjustChannel(g, contrastFactor);
                b = adjustChannel(b, contrastFactor);

                // Tworzymy nowy kolor i ustawiamy piksel w obrazie wyjściowym
                Color newColor = new Color(r, g, b, color.getAlpha());
                output.setRGB(x, y, newColor.getRGB());
            }
        }

        return output;
    }

    private static int adjustChannel(int value, double contrastFactor) {
        // Normalizacja wartości koloru do zakresu 0-1
        double normalized = value / 255.0;

        // Przesunięcie do środka (0.5) i zastosowanie kontrastu
        normalized = 0.5 + (normalized - 0.5) * (1 + contrastFactor);

        // Przycięcie do zakresu 0-1
        normalized = Math.max(0, Math.min(1, normalized));

        // Skalowanie z powrotem do zakresu 0-255
        return (int) (normalized * 255);
    }

    private static int clamp(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
}
