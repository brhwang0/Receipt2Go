package com.example.receipt2go;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class PrintReceipt {

    public static void print() {

        if (MainActivity.BLUETOOTH_PRINTER.IsNoConnection()){
            Toast.makeText(MainActivity.CONTEXT, R.string.print_error, Toast.LENGTH_SHORT).show();
        }

        MainActivity.BLUETOOTH_PRINTER.Begin();
        MainActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 1);//CENTER
        MainActivity.BLUETOOTH_PRINTER.SetLineSpacing((byte) 30);	//30 * 0.125mm
        MainActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte) 0x00);//normal
        MainActivity.BLUETOOTH_PRINTER.printString("Test");
    }
}
