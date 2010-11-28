package com.venitaxi.taxi;


import android.app.Activity;
import android.os.Bundle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class customlistview extends Activity {

 private static class EfficientAdapter extends BaseAdapter {
 private LayoutInflater mInflater;

 public EfficientAdapter(Context context) {
 mInflater = LayoutInflater.from(context);

 }

 public int getCount() {
 return country.length;
 }

 public Object getItem(int position) {
 return position;
 }

 public long getItemId(int position) {
 return position;
 }

 public View getView(int position, View convertView, ViewGroup parent) {
 ViewHolder holder;
 if (convertView == null) {
 convertView = mInflater.inflate(R.layout.listview, null);
 holder = new ViewHolder();
 holder.text = (TextView) convertView
 .findViewById(R.id.TextView01);
 holder.text2 = (TextView) convertView
 .findViewById(R.id.TextView02);

 convertView.setTag(holder);
 } else {
 holder = (ViewHolder) convertView.getTag();
 }

 holder.text.setText(curr[position]);
 holder.text2.setText(country[position]);

 return convertView;
 }

 static class ViewHolder {
 TextView text;
 TextView text2;
 }
 }

 @Override
 public void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 setContentView(R.layout.main);
 ListView l1 = (ListView) findViewById(R.id.ListView01);
 l1.setAdapter(new EfficientAdapter(this));
 }

 private static final String[] country = { "Iceland", "India", "Indonesia",
 "Iran", "Iraq", "Ireland", "Israel", "Italy", "Laos", "Latvia",
 "Lebanon", "Lesotho ", "Liberia", "Libya", "Lithuania",
 "Luxembourg" };
 private static final String[] curr = { "ISK", "INR", "IDR", "IRR", "IQD",
 "EUR", "ILS", "EUR", "LAK", "LVL", "LBP", "LSL ", "LRD", "LYD",
 "LTL ", "EUR"

 };

}
