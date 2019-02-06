package com.msahil432.sms.helpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;

import androidx.core.content.ContextCompat;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

/**
 * Created by sahil on 21/6/17.
 *
 * This will contain static methods to help with image operations
 */

public class ImageHelper {

  private ImageHelper(){}

  public static Drawable getTextDrawable(String text, int color){
    if(text.length()==1) return getAlphabet(text.charAt(0), color);
    TextDrawable.IBuilder builder = TextDrawable.builder()
        .beginConfig()
        .width(90)
        .height(90)
        .endConfig()
        .round();
    return builder.build(text, color);
  }

  private static ColorGenerator generator = ColorGenerator.MATERIAL;
  public static Drawable getTextDrawable(String text){
    int color = generator.getColor(text);
    return getTextDrawable(text, color);
  }

  public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {

    bitmap = getResizedBitmap(bitmap, 90);
    Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
        bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);

    final int color = 0xff424242;
    final Paint paint = new Paint();
    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    final RectF rectF = new RectF(rect);
    final float roundPx = bitmap.getWidth()/2;

    paint.setAntiAlias(true);
    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(color);
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(bitmap, rect, rect, paint);

    return output;
  }

  public static Bitmap getBitmapFromDrawable(Drawable drawable){
    Bitmap bitmap;
    if(drawable instanceof BitmapDrawable){
      BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
      if(bitmapDrawable.getBitmap()!=null){
        return bitmapDrawable.getBitmap();
      }
    }
    if(drawable.getIntrinsicWidth()<=0||drawable.getIntrinsicHeight()<=0){
      bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    }else{
      bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
          Bitmap.Config.ARGB_8888);
    }

    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0,0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);
    return bitmap;
  }

  public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
    int width = image.getWidth();
    int height = image.getHeight();

    float bitmapRatio = (float) width / (float) height;
    if (bitmapRatio > 1) {
      width = maxSize;
      height = (int) (width / bitmapRatio);
    } else {
      height = maxSize;
      width = (int) (height * bitmapRatio);
    }

    return Bitmap.createScaledBitmap(image, width, height, true);
  }
  public static Bitmap getBitmap(Context context, int drawableId) {
    Drawable drawable = ContextCompat.getDrawable(context, drawableId);
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    } else if (drawable instanceof VectorDrawable) {
      return getBitmap((VectorDrawable) drawable);
    } else {
      throw new IllegalArgumentException("unsupported drawable type");
    }
  }
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static Bitmap getBitmap(VectorDrawable vectorDrawable) {
    Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
        vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    vectorDrawable.draw(canvas);
    return bitmap;
  }

  private static TextDrawable.IBuilder builder = TextDrawable.builder()
      .beginConfig()
      .width(90)
      .height(90)
      .endConfig()
      .round();
    /*
    static int color = 395195;
    private static Drawable a = builder.build("A", color);
    private static Drawable b = builder.build("B", color);
    private static Drawable c = builder.build("C", color);
    private static Drawable d = builder.build("D", color);
    private static Drawable e = builder.build("E", color);
    private static Drawable f = builder.build("F", color);
    private static Drawable g = builder.build("G", color);
    private static Drawable h = builder.build("H", color);
    private static Drawable i = builder.build("I", color);
    private static Drawable j = builder.build("J", color);
    private static Drawable k = builder.build("K", color);
    private static Drawable l = builder.build("L", color);
    private static Drawable m = builder.build("M", color);
    private static Drawable n = builder.build("N", color);
    private static Drawable o = builder.build("O", color);
    private static Drawable p = builder.build("P", color);
    private static Drawable q = builder.build("Q", color);
    private static Drawable r = builder.build("R", color);
    private static Drawable s = builder.build("S", color);
    private static Drawable t = builder.build("T", color);
    private static Drawable u = builder.build("U", color);
    private static Drawable v = builder.build("V", color);
    private static Drawable w = builder.build("W", color);
    private static Drawable x = builder.build("X", color);
    private static Drawable y = builder.build("Y", color);
    private static Drawable z = builder.build("Z", color);
    private static Drawable num = builder.build("#", color);
    */

  public static Drawable getAlphabet(char chr, int color){
    if(color!=0){
      return builder.build(chr+"", color);
    }else{
      return builder.build(chr+"", generator.getColor(chr+""));
    }
        /*
        switch (chr){
            case 'A':   return a;
            case 'B':   return b;
            case 'C':   return c;
            case 'D':   return d;
            case 'E':   return e;
            case 'F':   return f;
            case 'G':   return g;
            case 'H':   return h;
            case 'I':   return i;
            case 'J':   return j;
            case 'K':   return k;
            case 'L':   return l;
            case 'M':   return m;
            case 'N':   return n;
            case 'O':   return o;
            case 'P':   return p;
            case 'Q':   return q;
            case 'R':   return r;
            case 'S':   return s;
            case 'T':   return t;
            case 'U':   return u;
            case 'V':   return v;
            case 'W':   return w;
            case 'X':   return x;
            case 'Y':   return y;
            case 'Z':   return z;
            default:    return num;
        }
        */
  }
}