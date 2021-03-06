package com.example.wishadish.ui.home;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.wishadish.DataBases.AppExecutors;
import com.example.wishadish.DataBases.CompleteMenuTable;
import com.example.wishadish.DataBases.MenuDb;
import com.example.wishadish.MainActivity;
import com.example.wishadish.MenuItemClass;
import com.example.wishadish.R;
import com.example.wishadish.TableInfoClass;
import com.example.wishadish.Utility.MySingleton;
import com.example.wishadish.ui.OrderOverview.OrderOverviewActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.wishadish.LoginSessionManager.EMP_ID;
import static com.example.wishadish.LoginSessionManager.EMP_TOKEN;
import static com.example.wishadish.LoginSessionManager.PREF_NAME;
import static com.example.wishadish.ui.Reports.ReportsFragment.BASE_URL;
import static com.example.wishadish.ui.Reports.ReportsFragment.NO_OF_RETRY;
import static com.example.wishadish.ui.Reports.ReportsFragment.RETRY_SECONDS;
import static com.example.wishadish.ui.Settings.SettingsFragment.SETTINGS_PREF;
import static com.example.wishadish.ui.Settings.SettingsFragment.TABLE_MODE;

public class HomeFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();
    private RecyclerView recyclerView1;
    private MenuItemAdapter adapter1;
    private List<MenuItemClass> menuItems;
    private LinearLayout menuModeLL;
    private RelativeLayout totalAmountBtnRL;
    private SearchView searchView;
    private String previousQuertText;
    private ListView searchLV;
    private LinearLayout totalAmountLL;
    private TextView totalAmountTv;
    private LinearLayout searchviewLL;

    private List<TableInfoClass> tableList;
    private RecyclerView recyclerView2;
    private RecyclerView.Adapter adapter2;
    private LinearLayout tableModeLL;
    private GridView grid;

    private List<CompleteMenuTable> completeMenuList;
    //This arraylist will have data as pulled from server. This will keep cumulating.
    private List<MenuItemClass> productResults;
    //Based on the search string, only filtered products will be moved here from productResults
    private List<MenuItemClass> filteredProductResults;

    private MenuDb mDb;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull final LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_home, container, false);

        ((MainActivity) getActivity()).setActionBarTitle("Home");

        setHasOptionsMenu(true);

        mDb = MenuDb.getInstance(getContext().getApplicationContext());

        getMenuList();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SETTINGS_PREF, Context.MODE_PRIVATE);
        boolean b = sharedPreferences.getBoolean(TABLE_MODE, false);

        Log.e(TAG, "table mode = " + b);

        if (b) {

            tableModeLL = root.findViewById(R.id.homeTableTypeLL);
            menuModeLL = root.findViewById(R.id.homeMenuTypeLL);
            grid = (GridView) root.findViewById(R.id.simpleGridView);

            tableModeLL.setVisibility(View.VISIBLE);
            menuModeLL.setVisibility(View.GONE);

            tableList = new ArrayList<>();

            getTableList();

        } else {

            tableModeLL = root.findViewById(R.id.homeTableTypeLL);
            menuModeLL = root.findViewById(R.id.homeMenuTypeLL);
            totalAmountLL = root.findViewById(R.id.totalAmountLL);
            totalAmountTv = root.findViewById(R.id.totalAmountTv);
            searchviewLL = root.findViewById(R.id.rl3);

            menuModeLL.setVisibility(View.VISIBLE);
            tableModeLL.setVisibility(View.GONE);
            totalAmountLL.setVisibility(View.GONE);

            searchView = root.findViewById(R.id.searchView);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    searchView.clearFocus();
                }
            }, 100);

            menuItems = new ArrayList<>();
            productResults = new ArrayList<>();
            filteredProductResults = new ArrayList<>();

            searchView = root.findViewById(R.id.searchView);
            totalAmountBtnRL = root.findViewById(R.id.totalAmountBtnRL);
            searchLV = root.findViewById(R.id.listview);
            recyclerView1 = root.findViewById(R.id.rv1);
            recyclerView1.setLayoutManager(new LinearLayoutManager(getContext()));

            adapter1 = new MenuItemAdapter(menuItems, getActivity().getApplicationContext(), totalAmountLL, totalAmountTv);
            recyclerView1.setAdapter(adapter1);

            searchView.setIconified(false);

            totalAmountBtnRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

//                    int size = ((MenuItemAdapter) recyclerView1.getAdapter()).getItemCount();
//                    for (int i = 0; i < size; i++) {
//                        // Get each selected item
//                        // Do something with the item like save it to a selected items array.
//                    }

                    String totaAmt = ((TextView) root.findViewById(R.id.totalAmountTv)).getText().toString();

                    List<MenuItemClass> itemsInList = new ArrayList<>();
                    itemsInList = ((MenuItemAdapter) recyclerView1.getAdapter()).getListItems();

                    Log.e(TAG, "size = " + itemsInList.size());

                    Intent intent = new Intent(getActivity(), OrderOverviewActivity.class);
                    intent.putExtra("list", (Serializable) itemsInList);
                    intent.putExtra("totAmt", totaAmt);
                    startActivity(intent);
                }
            });

            searchviewLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, "clicked!");
                }
            });

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchLV.setVisibility(View.VISIBLE);
                    productResults.clear();
                    displaySearchResults("");
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {

                    newText = newText.trim();

                    Log.e(TAG, "newText ="+newText+"abc");

                    if (newText.length() > 2 && !(newText.substring(0, 3).equalsIgnoreCase(previousQuertText))) {
                        searchLV.setVisibility(View.VISIBLE);
                        productResults.clear();
                        displaySearchResults(newText);
                        previousQuertText = newText.substring(0, 3);
                        Log.e(TAG, "ptext = " + previousQuertText);
                        Log.e(TAG, "1  newText=" + newText + "  " + "previousText=" + previousQuertText + "  productResults.size()= " + productResults.size());
                    } else if (newText.length() > 2 && newText.substring(0, 3).equalsIgnoreCase(previousQuertText)) {
                        searchLV.setVisibility(View.VISIBLE);
                        displaySearchResultFromPreviousList(newText);
                        Log.e(TAG, "2  newText=" + newText + "  " + "previousText=" + previousQuertText + "  productResults.size()= " + productResults.size());
                    } else if (newText.length() <= 2) {
                        filteredProductResults.clear();
                        searchLV.setVisibility(View.GONE);
                        Log.e(TAG, "3  newText=" + newText + "  " + "previousText=" + previousQuertText + "  productResults.size()= " + productResults.size());
                    }

                    return false;
                }
            });
        }

        return root;
    }

    class MyAdapter extends BaseAdapter {

        ArrayList<TableInfoClass> list;
        private Context context;

        MyAdapter(Context context) {
            this.context = context;
            list = new ArrayList();
            Log.e(TAG, "tablelist size = "+tableList.size());
            for (int count = 0; count < tableList.size(); count++) {
                TableInfoClass tempSchedule = new TableInfoClass(tableList.get(count).getmTableNo(), tableList.get(count).getmTableSize(), true);
                list.add(tempSchedule);
            }
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            TableInfoClass temptableInfo = list.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.table_mode_item, parent, false);
            }
            final TextView tableNumber = (TextView) convertView.findViewById(R.id.tableModeTableNumberTv);
            RelativeLayout tableLL = (RelativeLayout) convertView.findViewById(R.id.tableInGridLL);
            tableNumber.setText("Table No. #" + temptableInfo.getmTableNo());

            tableLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), list.get(position).getmTableNo() + " clicked!", Toast.LENGTH_SHORT).show();
                }
            });
            return convertView;
        }
    }

    private void displaySearchResultFromPreviousList(String itemname) {
        filterProductArray(itemname);
        searchLV.setAdapter(new SearchResultsAdapter(getActivity(), filteredProductResults, searchLV, adapter1, searchView));
    }

    private void displaySearchResults(final String itemname) {

        final MenuDb appDb = MenuDb.getInstance(getContext());

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                completeMenuList = appDb.completeMenuTableDao().getAllItems();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "complete menu size = " + completeMenuList.size());

                        for (int i = 0; i < completeMenuList.size(); i++) {
                            String name = completeMenuList.get(i).getName();
                            double rate = Double.parseDouble(completeMenuList.get(i).getRate());
                            String type = completeMenuList.get(i).getVeg();
                            String id = completeMenuList.get(i).getId();
                            String unit = completeMenuList.get(i).getUnit();
                            double gst_per = Double.parseDouble(completeMenuList.get(i).getGst_per());

                            MenuItemClass tempItem = new MenuItemClass(name, 0, type, rate, id, unit, gst_per);

                            String matchfound = "N";

                            for (int j = 0; i < productResults.size(); j++) {
                                if (productResults.get(j).getmItemName().equals(tempItem.getmItemName())) {
                                    matchfound = "Y";
                                    break;
                                }
                            }

                            if (matchfound.equals("N")) {
                                productResults.add(tempItem);
                            }
                        }

                        //calling this method to filter the search results from productResults and move them to
                        //filteredProductResults
                        filterProductArray(itemname);
                        searchLV.setAdapter(new SearchResultsAdapter(getActivity(), filteredProductResults, searchLV, adapter1, searchView));
                    }
                });
            }
        });

    }

    private void getMenuList() {

        Log.e(TAG, "called : getMenuList()");

        final String GET_MENU_URL = BASE_URL + "/dashboard/search";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_MENU_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e(TAG, response);

                try {

                    JSONObject jsonResponse = new JSONObject(response);
                    int code = jsonResponse.getInt("code");

                    if (code != 1) {
                        Toast.makeText(getContext(), "code + " + code, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONArray itemlist = jsonResponse.getJSONArray("menu");

                    completeMenuList = new ArrayList<>();
                    final MenuDb appDb = MenuDb.getInstance(getContext());

                    for (int i = 0; i < itemlist.length(); i++) {

                        JSONObject jo = itemlist.getJSONObject(i);

                        String id = jo.getString("id");
                        String name = jo.getString("name");
                        String rate = jo.getString("rate");
                        String veg = jo.getString("veg");
                        String unit = jo.getString("unit");
                        String gst_per = jo.getString("gst_per");
                        String ttype_tag = jo.getString("ttype_tag");
                        String geo_tag = jo.getString("geo_tag");
                        String time_created = jo.getString("time_created");
                        String status = jo.getString("status");

                        if (veg == "1")
                            veg = "veg";
                        else if (veg == "0")
                            veg = "non-veg";

                        final CompleteMenuTable x = new CompleteMenuTable(name, id, unit, rate, gst_per, ttype_tag, geo_tag, veg, time_created, status);

                        final int finalI = i;
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                appDb.completeMenuTableDao().insert(x);
//                                getActivity().runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Log.e(TAG, "inserted "+ finalI);
//                                    }
//                                });
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "getMenuList : Exception caught  " + e);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error.toString());
                Toast.makeText(getContext(), "Error in getMenuList() !", Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String mid = sharedPreferences.getString(EMP_ID, "");
                params.put("name", "");
                params.put("merchant_id", mid);

                Log.e("merchant_id", "it is = " + mid);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String ACCESS_TOKEN = sharedPreferences.getString(EMP_TOKEN, "");
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("x-access-token", ACCESS_TOKEN);

                Log.e("x-access-token", "It is = " + ACCESS_TOKEN);

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS, NO_OF_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
                Fragment myFragment = new waitlistFrag();
                getActivity().getSupportFragmentManager().beginTransaction().add(android.R.id.content, myFragment).commit();
                Log.e("clicked", "yesss");
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void filterProductArray(String newText) {
        String pName;
        filteredProductResults.clear();
        for (int i = 0; i < productResults.size(); i++) {
            pName = productResults.get(i).getmItemName().toLowerCase();
            if (pName.contains(newText.toLowerCase())) {
                filteredProductResults.add(productResults.get(i));
            }
        }
    }

    private void getTableList() {

        Log.e(TAG, "called : loadWaitingList()");

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        String GET_TABLES_URL = BASE_URL + "/dashboard";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, GET_TABLES_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.e(TAG, response);

                progressDialog.dismiss();

                try {

                    JSONObject jsonResponse = new JSONObject(response);
                    int code = jsonResponse.getInt("code");

                    if (code != 1) {
                        Toast.makeText(getContext(), "code + " + code, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONArray array1 = jsonResponse.getJSONArray("tables");

                    for (int i = 0; i < array1.length(); i++) {

                        JSONObject jo = array1.getJSONObject(i);

                        String id = jo.getString("id");
                        int table_id = Integer.parseInt(jo.getString("table_id"));
                        int size = Integer.parseInt(jo.getString("size"));
                        String date = jo.getString("date");
                        String status = jo.getString("status");
                        String active = jo.getString("active");

                        Log.e("active", ""+id+"  "+active);

                        boolean activeBool;
                        if (active.equals("1")) {
                            TableInfoClass tempItem = new TableInfoClass(table_id, size, true);
                            tableList.add(tempItem);
                        }
                    }

                    Log.e(TAG, "tableList = "+tableList.size());

                    MyAdapter myAdapter = new MyAdapter(getActivity());
                    grid.setAdapter(myAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "loadWaitlist : Exception caught  " + e);
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error.toString());
                Toast.makeText(getContext(), "Error in loadWaitingList() !", Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String ACCESS_TOKEN = sharedPreferences.getString(EMP_TOKEN, "");
                params.put("Content-Type", "application/x-www-form-urlencoded");
                params.put("x-access-token", ACCESS_TOKEN);

                Log.e("x-access-token", "It is = " + ACCESS_TOKEN);

                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();

                SharedPreferences sharedPreferences = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                String mid = sharedPreferences.getString(EMP_ID, "");
                params.put("merchant_id", mid);

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(RETRY_SECONDS, NO_OF_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getContext()).addToRequestQueue(stringRequest);
    }
}