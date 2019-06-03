package com.inspur.emmcloud.ui.find.trip;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.FindAPIService;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.bean.find.GetTripArriveCity;
import com.inspur.emmcloud.bean.find.Trip;
import com.inspur.emmcloud.ui.contact.ContactSearchActivity;
import com.inspur.emmcloud.util.privates.NetUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.MyDatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripDetailActivity extends BaseActivity {
    private static final int SELECT_RIDER = 1;
    private TextView tripDateEdit;
    private EditText tripFromEdit;
    private EditText tripDestinationEdit;
    private TextView tripStartTimeEdit;
    private TextView tripArriveDateEdit;
    private TextView tripArriveTimeEdit;
    private EditText trainIDEdit;
    private EditText seatIDEdit;
    private TextView riderEdit;
    private EditText orderNumEdit;
    private EditText costEdit;
    private EditText tripStartCityEdit;
    private EditText tripArriveCityEdit;
    private Trip trip;
    private String rider;
    private String riderUid;
    private LoadingDialog loadingDlg;
    private Trip uploadTrip;
    private FindAPIService apiService;


    @Override
    public void onCreate() {
        initView();
        initData();
        apiService = new FindAPIService(TripDetailActivity.this);
        apiService.setAPIInterface(new WebService());
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_trip_detail;
    }

    private void initView() {
        // TODO Auto-generated method stub
        tripDateEdit = (TextView) findViewById(R.id.trip_date_edit);
        tripFromEdit = (EditText) findViewById(R.id.trip_from_edit);
        tripDestinationEdit = (EditText) findViewById(R.id.trip_destination_edit);
        tripStartTimeEdit = (TextView) findViewById(R.id.trip_start_time_edit);
        tripArriveDateEdit = (TextView) findViewById(R.id.trip_arrive_date_edit);
        tripArriveTimeEdit = (TextView) findViewById(R.id.trip_arrive_time_edit);
        trainIDEdit = (EditText) findViewById(R.id.trip_train_id_edit);
        seatIDEdit = (EditText) findViewById(R.id.trip_seat_id_edit);
        riderEdit = (TextView) findViewById(R.id.trip_rider_edit);
        orderNumEdit = (EditText) findViewById(R.id.trip_order_num_edit);
        costEdit = (EditText) findViewById(R.id.trip_cost_edit);
        tripStartCityEdit = (EditText) findViewById(R.id.trip_from_city_edit);
        tripArriveCityEdit = (EditText) findViewById(R.id.trip_destination_city_edit);
        loadingDlg = new LoadingDialog(TripDetailActivity.this);
    }

    private void initData() {
        // TODO Auto-generated method stub
        trip = (Trip) getIntent().getExtras().getSerializable("trip");
        tripDateEdit.setText(trip.getStartDate(getApplicationContext()));
        tripFromEdit.setText(trip.getFrom());
        tripDestinationEdit.setText(trip.getDestination());
        tripStartTimeEdit.setText(trip.getStartTime(getApplicationContext()));
        tripArriveDateEdit.setText(trip.getEndDate(getApplicationContext()));
        tripArriveTimeEdit.setText(trip.getEndTime(getApplicationContext()));
        trainIDEdit.setText(trip.getNumber());
        seatIDEdit.setText(trip.getSeatnumber());
        tripStartCityEdit.setText(trip.getFromCity());
        tripArriveCityEdit.setText(trip.getDestinationCity());
        rider = trip.getUname();
        riderUid = trip.getUid();
        if (!StringUtils.isBlank(riderUid)) {
            riderEdit.setText(rider);
        }
        orderNumEdit.setText(trip.getOrderID());
        float cost = trip.getCost();
        String costStr = "";
        if (cost == -1) {
            costStr = "";
        } else {
            costStr = cost + "";
        }
        costEdit.setText(costStr);

        tripArriveCityEdit
                .setOnFocusChangeListener(new OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        // TODO Auto-generated method stub
                        if (hasFocus
                                && NetUtils
                                .isNetworkConnected(TripDetailActivity.this)
                                && !TextUtils.isEmpty(tripDestinationEdit
                                .getText().toString())) {
                            loadingDlg.show();
                            apiService.getArriveCity(tripDestinationEdit
                                    .getText().toString());
                        }

                    }
                });

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.trip_date_edit:
                showDatePickerDlg(true);
                break;
            case R.id.trip_arrive_date_edit:
                showDatePickerDlg(false);
                break;
            case R.id.trip_start_time_edit:
                showTimePickerDlg(true);
                break;
            case R.id.trip_arrive_time_edit:
                showTimePickerDlg(false);
                break;
            case R.id.trip_rider_edit:
                Intent intent = new Intent();
                intent.putExtra("select_content", 2);
                intent.putExtra("isMulti_select", false);
                intent.putExtra("title", getString(R.string.choose_rider));
                intent.putExtra("isContainMe", true);
                intent.setClass(getApplicationContext(),
                        ContactSearchActivity.class);
                startActivityForResult(intent, SELECT_RIDER);
                break;
            case R.id.save_text:
                String tripDate = tripDateEdit.getText().toString();
                String tripStartCity = tripStartCityEdit.getText().toString();
                String tripFrom = tripFromEdit.getText().toString();
                String tripDestinationCity = tripArriveCityEdit.getText()
                        .toString();
                String tripDestination = tripDestinationEdit.getText().toString();
                String tripStartTime = tripStartTimeEdit.getText().toString();
                String tripArriveDate = tripArriveDateEdit.getText().toString();
                String tripArriveTime = tripArriveTimeEdit.getText().toString();
                String trainID = trainIDEdit.getText().toString();
                String seatID = seatIDEdit.getText().toString();
                rider = riderEdit.getText().toString();
                String orderNum = orderNumEdit.getText().toString();
                String cost = costEdit.getText().toString();
                if (StringUtils.isBlank(tripDate)) {
                    ToastUtils.show(getApplicationContext(), R.string.please_input_trip_date);
                    return;
                }
                if (StringUtils.isBlank(tripStartCity)) {
                    ToastUtils.show(getApplicationContext(), R.string.please_input_trip_start_city);
                    return;
                }
                if (StringUtils.isBlank(tripFrom)) {
                    ToastUtils.show(getApplicationContext(), R.string.please_input_trip_from);
                    return;
                }
                if (StringUtils.isBlank(tripDestinationCity)) {
                    ToastUtils.show(getApplicationContext(), R.string.please_input_trip_arrive_city);
                    return;
                }
                if (StringUtils.isBlank(tripDestination)) {
                    ToastUtils.show(getApplicationContext(), R.string.please_input_trip_end);
                    return;
                }
                if (StringUtils.isBlank(tripStartTime)) {
                    ToastUtils.show(getApplicationContext(), R.string.please_input_trip_start_time);
                    return;
                }

                if (StringUtils.isBlank(tripArriveDate)) {
                    ToastUtils.show(getApplicationContext(), R.string.please_input_trip_arrive_date);
                    return;
                }
                if (StringUtils.isBlank(tripArriveTime)) {
                    ToastUtils.show(getApplicationContext(), R.string.please_input_trip_arrive_time);
                    return;
                }
                if (StringUtils.isBlank(trainID)) {
                    ToastUtils.show(getApplicationContext(), R.string.please_input_train_num);
                    return;
                }
                if (StringUtils.isBlank(seatID)) {
                    ToastUtils.show(getApplicationContext(), R.string.please_input_seat_num);
                    return;
                }
                if (StringUtils.isBlank(rider)) {
                    ToastUtils.show(getApplicationContext(), R.string.please_input_rider);
                    return;
                }
                if (StringUtils.isBlank(orderNum)) {
                    ToastUtils.show(getApplicationContext(), R.string.please_input_order_num);
                    return;
                }
                if (StringUtils.isBlank(cost)) {
                    ToastUtils.show(getApplicationContext(), R.string.please_input_ticket_cost);
                    return;
                }
                String startDateStr = tripDate + " " + tripStartTime;
                String arriveDateStr = tripArriveDate + " " + tripArriveTime;
                Date startDate = TimeUtils.timeString2Date(getApplicationContext(), startDateStr, TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE);
                Date arriveDate = TimeUtils.timeString2Date(getApplicationContext(), arriveDateStr, TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE);
                if (arriveDate.before(startDate)) {
                    ToastUtils.show(getApplicationContext(), R.string.trip_date_or_time_error);
                    return;
                }
                uploadTripInfo(startDate, arriveDate, tripStartCity, tripFrom,
                        tripDestinationCity, tripDestination, trainID, seatID,
                        rider, orderNum, cost);
                break;
            case R.id.trip_destination_city_edit:

                break;

            default:
                break;
        }
    }

    private void showTimePickerDlg(final boolean isStartTime) {
        Calendar currentCalendar = Calendar.getInstance();
        int hourOfDay = currentCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = currentCalendar.get(Calendar.MINUTE);
        String startTime = "";
        if (StringUtils.isBlank(tripStartTimeEdit.getText().toString())) {
            startTime = trip.getStartTime(getApplicationContext());
        } else {
            startTime = tripStartTimeEdit.getText().toString();
        }
        String[] startTimeArray = startTime.split(":");
        if (isStartTime) {
            if (startTimeArray != null && startTimeArray.length == 2) {
                hourOfDay = Integer.valueOf(startTimeArray[0]);
                minute = Integer.valueOf(startTimeArray[1]);
            }
        } else {
            String arriveTime = "";
            if (StringUtils.isBlank(tripArriveTimeEdit.getText().toString())) {
                arriveTime = trip.getEndTime(getApplicationContext());
            } else {
                arriveTime = tripArriveTimeEdit.getText().toString();
            }
            String[] endTimeArray = arriveTime.split(":");
            if (endTimeArray != null && endTimeArray.length == 2) {
                hourOfDay = Integer.valueOf(endTimeArray[0]);
                minute = Integer.valueOf(endTimeArray[1]);
            } else if (startTimeArray != null && startTimeArray.length == 2) {
                hourOfDay = Integer.valueOf(startTimeArray[0]);
                minute = Integer.valueOf(startTimeArray[1]);
            }
        }

        // TODO Auto-generated method stub
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                TripDetailActivity.this, new OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay,
                                  int minute) {
                // TODO Auto-generated method stub
                String hourOfDayStr = hourOfDay + "";
                if (hourOfDay < 10) {
                    hourOfDayStr = "0" + hourOfDayStr;
                }

                String minuteStr = minute + "";
                if (minute < 10) {
                    minuteStr = "0" + minuteStr;
                }
                if (isStartTime) {
                    tripStartTimeEdit.setText(hourOfDayStr + ":"
                            + minuteStr);
                } else {
                    tripArriveTimeEdit.setText(hourOfDayStr + ":"
                            + minuteStr);
                }
            }
        }, hourOfDay, minute, true);
        timePickerDialog.show();
    }

    /**
     * 弹出行程日期选择框
     */
    private void showDatePickerDlg(final boolean isStartDate) {
        // TODO Auto-generated method stub
        Calendar currentCalendar = Calendar.getInstance();
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH);
        int day = currentCalendar.get(Calendar.DAY_OF_MONTH);

        String[] startDateArray = tripDateEdit.getText().toString().split("-");
        if (isStartDate) {
            if (startDateArray != null && startDateArray.length == 3) {
                year = Integer.valueOf(startDateArray[0]);
                month = Integer.valueOf(startDateArray[1]);
                day = Integer.valueOf(startDateArray[2]);
            }

        } else {
            String[] endDateArray = tripArriveDateEdit.getText().toString()
                    .split("-");
            if (endDateArray != null && endDateArray.length == 3) {
                year = Integer.valueOf(endDateArray[0]);
                month = Integer.valueOf(endDateArray[1]);
                day = Integer.valueOf(endDateArray[2]);
            } else if (startDateArray != null && startDateArray.length == 3) {
                year = Integer.valueOf(startDateArray[0]);
                month = Integer.valueOf(startDateArray[1]);
                day = Integer.valueOf(startDateArray[2]);
            }
        }
        Locale locale = getResources().getConfiguration().locale;
        Locale.setDefault(locale);
        MyDatePickerDialog datePickerDialog = new MyDatePickerDialog(
                TripDetailActivity.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        // TODO Auto-generated method stub
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, monthOfYear, dayOfMonth);
                        String tripDate = TimeUtils.calendar2FormatString(getApplicationContext(), calendar, TimeUtils.FORMAT_YEAR_MONTH_DAY);
                        if (isStartDate) {
                            tripDateEdit.setText(tripDate);
                        } else {
                            tripArriveDateEdit.setText(tripDate);
                        }

                    }
                }, year, month - 1, day);
        datePickerDialog.show();
    }

    /**
     * 上传完整的行程信息
     *
     * @param tripStartDate
     * @param tripArriveDate
     * @param tripStartCity
     * @param tripFrom
     * @param tripDestinationCity
     * @param tripDestination
     * @param trainID
     * @param seatID
     * @param rider
     * @param orderNum
     * @param cost
     */
    private void uploadTripInfo(Date tripStartDate, Date tripArriveDate,
                                String tripStartCity, String tripFrom, String tripDestinationCity,
                                String tripDestination, String trainID, String seatID,
                                String rider, String orderNum, String cost) {
        // TODO Auto-generated method stub
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            List<Trip> uploadTicketInfoList = new ArrayList<Trip>();
            uploadTrip = new Trip();
            uploadTrip.setTid(trip.getTid());
            uploadTrip.setDestination(tripDestination);
            uploadTrip.setLevel(trip.getLevel());
            uploadTrip.setCost(Float.valueOf(cost));
            uploadTrip.setFrom(tripFrom);
            uploadTrip.setNumber(trainID);
            uploadTrip.setOrderID(orderNum);
            uploadTrip.setSeatnumber(seatID);
            uploadTrip.setSendDate(trip.getSendDate());
            uploadTrip.setSource(trip.getSource());
            uploadTrip.setSourceuid(trip.getSourceuid());
            uploadTrip.setSourceuname(trip.getSourceuname());
            uploadTrip.setStart(tripStartDate);
            uploadTrip.setEnd(tripArriveDate);
            uploadTrip.setUid(riderUid);
            uploadTrip.setUname(rider);
            uploadTrip.setWay(trip.getWay());
            uploadTrip.setFromCity(tripStartCity);
            uploadTrip.setDestinationCity(tripDestinationCity);
            uploadTicketInfoList.add(uploadTrip);
            String uploadTicketInfos = JSONUtils.toJSONString(uploadTicketInfoList);
            // APIService apiService = new APIService(getApplicationContext());
            // apiService.setAPIInterface(new WebService());
            apiService.updateTrainTicket(uploadTicketInfos);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == SELECT_RIDER) {
            String result = data.getStringExtra("searchResult");
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (jsonObject.has("people")) {
                    JSONArray peopleArray = jsonObject.getJSONArray("people");
                    if (peopleArray.length() > 0) {
                        JSONObject peopleObj = peopleArray.getJSONObject(0);
                        if (peopleObj.has("pid")) {
                            riderUid = peopleObj.getString("pid");
                        }
                        if (peopleObj.has("name")) {
                            rider = peopleObj.getString("name");
                        }
                        riderEdit.setText(rider);
                    }

                }

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    private class WebService extends APIInterfaceInstance {

        @Override
        public void returnUploadTrainTicketSuccess() {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            ToastUtils.show(getApplicationContext(), R.string.save_success);
            Intent intent = new Intent();
            intent.putExtra("newTrip", uploadTrip);
            setResult(RESULT_OK, intent);
            Intent intentBroadCase = new Intent("refresh_trip_list");
            LocalBroadcastManager.getInstance(TripDetailActivity.this).sendBroadcast(intentBroadCase);
            finish();
        }

        @Override
        public void returnUploadTrainTicketFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            ToastUtils.show(getApplicationContext(), R.string.save_fail, errorCode);
        }

        @Override
        public void returnTripArriveSuccess(GetTripArriveCity getTripArriveCity) {
            // TODO Auto-generated method stub
            super.returnTripArriveSuccess(getTripArriveCity);

            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }

            if (!TextUtils.isEmpty(getTripArriveCity.getCityShortName())) {
                tripArriveCityEdit.setText(getTripArriveCity.getCityShortName());
                CharSequence text = tripArriveCityEdit.getText();
                if (text instanceof Spannable) {
                    Spannable spanText = (Spannable) text;
                    Selection.setSelection(spanText, text.length());
                }
            } else {
                tripArriveCityEdit.setText("");
                ToastUtils.show(getApplicationContext(), R.string.station_name_not_exist);
            }


        }

        @Override
        public void returnTripArriveFail(String error, int errorCode) {
            // TODO Auto-generated method stub
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(TripDetailActivity.this, error, errorCode);
        }

    }

}
