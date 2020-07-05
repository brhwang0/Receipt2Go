package com.example.receipt2go;

import android.app.Activity;

public class PrintReceipt {

    public static boolean print() {
        if(MainActivity.BLUETOOTH_PRINTER.IsNoConnection()){
            return false;
        }

        MainActivity.BLUETOOTH_PRINTER.Begin();
        MainActivity.BLUETOOTH_PRINTER.SetAlignMode((byte) 1);//CENTER
        MainActivity.BLUETOOTH_PRINTER.SetLineSpacing((byte) 30);	//30 * 0.125mm
        MainActivity.BLUETOOTH_PRINTER.SetFontEnlarge((byte) 0x00);//normal
        MainActivity.BLUETOOTH_PRINTER.BT_Write("Test");

    }
}
