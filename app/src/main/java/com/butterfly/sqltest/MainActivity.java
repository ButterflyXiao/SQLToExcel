package com.butterfly.sqltest;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import static com.butterfly.sqltest.FileUtil.copyFileUsingFileChannels;

public class MainActivity extends AppCompatActivity {
    TextView db_old_path;
    TextView db_new_path;
    TextView excel;
    Button open_db;
    Button change_btn;
    Button open_excel_btn;

    String old_path;
    String new_path;

    @SuppressLint("SdCardPath")
    String db_path="/data/data/com.butterfly.sqltest/databases/";
    String db_name;

    private String excel_name="db.xls";//生成表格名
    String excel_path = Environment.getExternalStorageDirectory().getPath()+"/backup/";//生成表格路径

    SQLiteToExcel sqliteToExcel;
    boolean isChange=false;//标志转换按钮是否可点击
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        open_db = findViewById(R.id.open_db);
        change_btn=findViewById(R.id.change_btn);
        open_excel_btn=findViewById(R.id.open_excel_btn);
        db_old_path =  findViewById(R.id.db_old_path);
        db_new_path =  findViewById(R.id.db_new_path);
        excel = findViewById(R.id.excel_path);

        open_db.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isChange=true;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });

        change_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isChange==true)
                    change(v);
            }
        });

        open_excel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent("android.intent.action.VIEW");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromFile(new File(excel_path+excel_name ));
                intent.setDataAndType(uri, "application/vnd.ms-excel");

                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
                old_path = uri.getPath();
                db_old_path.setText(old_path);
                Toast.makeText(this,old_path+"11111",Toast.LENGTH_SHORT).show();
                return;
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                old_path = FileUtil.getPath(this, uri);
                db_old_path.setText("原数据库文件路径:"+old_path);

                Toast.makeText(this,old_path,Toast.LENGTH_SHORT).show();
            } else {//4.4以下下系统调用方法
                old_path = FileUtil.getRealPathFromURI(this,uri);
                db_old_path.setText(old_path);
                Toast.makeText(MainActivity.this, old_path+"222222", Toast.LENGTH_SHORT).show();
            }
            File file=new File(old_path);
            db_name=file.getName();
            new_path=db_path+db_name;
            db_new_path.setText("目标文件路径："+new_path);
            excel.setText("生成表格路径："+excel_path+excel_name);
            try {//将文件复制到该项目数据库目录下，以进行读操作
                copyFileUsingFileChannels(new File(old_path),new File(new_path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void change(final View view){

        sqliteToExcel = new SQLiteToExcel(getApplicationContext(), db_name, excel_path);

        sqliteToExcel.exportAllTables(excel_name, new SQLiteToExcel.ExportListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onCompleted(String filePath) {
                Utils.showSnackBar(view, "Successfully Exported");
            }

            @Override
            public void onError(Exception e) {
                Utils.showSnackBar(view, e.getMessage());
            }
        });
    }
}