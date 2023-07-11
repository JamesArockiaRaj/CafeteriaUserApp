package com.example.atoscafe;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterShop extends Filter {
    private AdapterShop adapter;
    private ArrayList<ModelShop> filterList;

    public FilterShop(AdapterShop adapter, ArrayList<ModelShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //Validate Data for Search Query
        if(constraint!=null && constraint.length() > 0){
            //Search filled not empty, searching something, perform search

            //Change to uppercase, to make case insensitive
            constraint = constraint.toString().toUpperCase();
            //Store our filtered list
            ArrayList<ModelShop> filteredModels = new ArrayList<>();
            for(int i=0; i<filterList.size(); i++){
                //Check, search by title and category
//                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint)||
//                        filterList.get(i).getProductCategory().toUpperCase().contains(constraint)){
//                    //Add Filtered Data to List
//                    filteredModels.add(filterList.get(i));
//                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            //Search filled empty,not searching, return original/all/complete list

            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.shopList = (ArrayList<ModelShop>) results.values;
        //Refresh Adapter
        adapter.notifyDataSetChanged();
    }
}


