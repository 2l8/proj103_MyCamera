package com.example.user.proj103_mycamera;

        import android.app.Activity;
        import android.content.pm.ActivityInfo;
        import android.content.res.Configuration;
        import android.graphics.ImageFormat;
        import android.os.Bundle;
        import android.renderscript.ScriptIntrinsicYuvToRGB;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.ViewGroup.LayoutParams;
        import android.view.Window;
        import android.view.WindowManager;
        import android.widget.Button;
        import android.view.View;
        import android.util.Log;

        import android.hardware.Camera;
        import android.hardware.Camera.Parameters;
        import android.hardware.Camera.Size;
        import android.widget.TextView;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.text.DateFormat;
        import java.text.SimpleDateFormat;
        import java.util.Calendar;
        import java.util.List;


public class MainActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback, Camera.PreviewCallback, Camera.AutoFocusCallback
{
    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private SurfaceView preview;
    private Button shotBtn;
    private TextView TV1;
    private TextView TV2;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // если хотим, чтобы приложение постоянно имело портретную ориентацию
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // если хотим, чтобы приложение было полноэкранным
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // и без заголовка
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);


        // наше SurfaceView имеет имя SurfaceView01
        preview = (SurfaceView) findViewById(R.id.SurfaceView01);

        surfaceHolder = preview.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // кнопка имеет имя Button01
        shotBtn = (Button) findViewById(R.id.btnStartStop);
        shotBtn.setText("Shot");
        shotBtn.setOnClickListener(this);

        // кнопка имеет имя Button01
        TV1 = (TextView) findViewById(R.id.btnStartStop);
        TV1.setText("0000");
        //shotBtn.setOnClickListener(this);

        // кнопка имеет имя Button01
        TV2 = (TextView) findViewById(R.id.textView2);
        TV2.setText("0000");
        //shotBtn.setOnClickListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        camera = Camera.open();


        Camera.Parameters params = camera.getParameters();
        List<Integer> formats = params.getSupportedPreviewFormats();
        for(int i: formats) {
            Log.e("myTAG", "preview format supported are = "+i);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (camera != null)
        {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        try
        {
            //Parameters param = camera.getParameters();
            //param.setPreviewFormat(ImageFormat.RGB_565);
            camera.setPreviewDisplay(holder);
            camera.setPreviewCallback(this);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Size previewSize = camera.getParameters().getPreviewSize();
        float aspect = (float) previewSize.width / previewSize.height;

        int previewSurfaceWidth = preview.getWidth();
        int previewSurfaceHeight = preview.getHeight();

        LayoutParams lp = preview.getLayoutParams();

        // здесь корректируем размер отображаемого preview, чтобы не было искажений

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
            // портретный вид
            camera.setDisplayOrientation(90);
            lp.height = 100;//previewSurfaceHeight/4;
            lp.width = 100;//(int) (previewSurfaceHeight / aspect);
            ;
        }
        else
        {
            // ландшафтный
            camera.setDisplayOrientation(0);
            lp.width = (int)(previewSurfaceWidth/3);
            lp.height = (int) (previewSurfaceWidth / (aspect*3));
        }

        preview.setLayoutParams(lp);
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }

    @Override
    public void onClick(View v)
    {
        if (v == shotBtn)
        {
            // либо делаем снимок непосредственно здесь
            // 	либо включаем обработчик автофокуса

            //camera.takePicture(null, null, null, this);
            camera.autoFocus(this);
        }
    }

    @Override
    public void onPictureTaken(byte[] paramArrayOfByte, Camera paramCamera)
    {
        // сохраняем полученные jpg в папке /sdcard/CameraExample/
        // имя файла - System.currentTimeMillis()

        try
        {
            File saveDir = new File("/sdcard/CameraExample/");

            if (!saveDir.exists())
            {
                saveDir.mkdirs();
            }

            FileOutputStream os = new FileOutputStream(String.format("/sdcard/CameraExample/%d.jpg", System.currentTimeMillis()));
            os.write(paramArrayOfByte);
            os.close();
        }
        catch (Exception e)
        {
        }

        // после того, как снимок сделан, показ превью отключается. необходимо включить его
        paramCamera.startPreview();
    }

    @Override
    public void onAutoFocus(boolean paramBoolean, Camera paramCamera)
    {
        if (paramBoolean)
        {
            // если удалось сфокусироваться, делаем снимок
            paramCamera.takePicture(null, null, null, this);
        }
    }

    @Override
    public void onPreviewFrame(byte[] paramArrayOfByte, Camera paramCamera)
    {
        int i=0;
        int r=0;
        for (i=0; i<((int)paramArrayOfByte.length); i=i+3) {
            r = r + paramArrayOfByte[i];
        }
        r=(int)(r/(i/3));
        TV1.setText(""+r);

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        if (r>30)
        {
            TV2.append(dateFormat.format(cal.getTime())+" " +r+"\n");
        }

    }
}