package com.ambinusian.adab.ui.student;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ambinusian.adab.R;
import com.ambinusian.adab.manager.APIManager;
import com.ambinusian.adab.manager.NetworkHelper;
import com.ambinusian.adab.preferences.UserPreferences;
import com.ambinusian.adab.recyclerview.classlist.ClassListAdapter;
import com.ambinusian.adab.recyclerview.classlist.ClassListModel;
import com.ambinusian.adab.recyclerview.schedule.ScheduleAdapter;
import com.ambinusian.adab.recyclerview.schedule.ScheduleModel;
import com.ambinusian.adab.room.ClassDatabase;
import com.ambinusian.adab.room.ClassEntity;
import com.ambinusian.adab.utility.DateUtility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.Calendar;

import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.model.CalendarEvent;
import devs.mulham.horizontalcalendar.utils.CalendarEventsPredicate;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;


public class FragmentCalendar extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_student_calendar, container, false);
    }

    TextView selectedDate, noClass;
    ArrayList<ScheduleModel> allClassSchedule;
    RecyclerView scheduleList;
    Date time;
    LinearLayout emptyClass;
    HorizontalCalendar.Builder builder;
    ClassDatabase db;
    UserPreferences userPreferences;
    Activity mainActivity;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        selectedDate = view.findViewById(R.id.tv_selectedDate);
        noClass = view.findViewById(R.id.tv_no_class);
        scheduleList = view.findViewById(R.id.rv_schedule);
        emptyClass = view.findViewById(R.id.empty_class_layout);
        allClassSchedule = new ArrayList<>();
        db = ClassDatabase.getDatabase(getContext());
        mainActivity = getActivity();
        userPreferences = new UserPreferences(getContext());

        //set text attributes
        setTextSize();
        setTextTypeface();

        db.classDAO().getAllClass().observe(getActivity(), new Observer<List<ClassEntity>>() {
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onChanged(List<ClassEntity> classEntities) {
                for(ClassEntity classEntity : classEntities){
                    allClassSchedule.add(new ScheduleModel(
                            classEntity.getSessionStartDate(),
                            (String) classEntity.getSessionMode(),
                            (String) classEntity.getTopicTitle(),
                            (String) classEntity.getCourseName(),
                            (String) classEntity.getCourseId(),
                            (String) classEntity.getClassName(),
                            DateUtility.convertToTimeFormat(classEntity.getSessionStartDate())));
                }

                /* starts before 10 years from now */
                Calendar startDate = Calendar.getInstance();
                startDate.add(Calendar.YEAR, -10);

                /* ends after 10 years from now */
                Calendar endDate = Calendar.getInstance();
                endDate.add(Calendar.YEAR, 10);

                builder = new HorizontalCalendar.Builder(mainActivity, R.id.calendarView).range(startDate, endDate)
                        .datesNumberOnScreen(5);

                HorizontalCalendar horizontalCalendar = builder.addEvents(new CalendarEventsPredicate() {
                    @Override
                    public List<CalendarEvent> events(Calendar date) {
                        //Searching the classes at selected date
                        ArrayList<ScheduleModel> selectedDateClasses = new ArrayList<>();
                        for (int i = 0; i < allClassSchedule.size(); i++) {
                            Calendar selectedDateCalendar = Calendar.getInstance();
                            selectedDateCalendar.setTime(DateUtility.convertStringToDate(allClassSchedule.get(i).getClassDate()));

                            if (selectedDateCalendar.equals(date)) {
                                ScheduleModel item = allClassSchedule.get(i);
                                selectedDateClasses.add(item);
                            }
                        }

                        List<CalendarEvent> events = new ArrayList<>();

                        for(int i = 0; i < selectedDateClasses.size();i++){
                            events.add(new CalendarEvent(getResources().getColor(R.color.colorAccent)));
                        }
                        return events;
                    }
                }).build();


                horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
                    @Override
                    public void onDateSelected(Calendar date, int position) {
                        selectedDate.setText(DateUtility.convertToDateFormat(date.getTime()));

                        //Searching the classes at selected date
                        ArrayList<ScheduleModel> selectedDateClasses = new ArrayList<>();
                        for (int i = 0; i < allClassSchedule.size(); i++) {
                            Calendar selectedDateCalendar = Calendar.getInstance();
                            selectedDateCalendar.setTime(DateUtility.convertStringToDate(allClassSchedule.get(i).getClassDate()));

                            if (selectedDateCalendar.equals(date)) {
                                ScheduleModel item = allClassSchedule.get(i);
                                selectedDateClasses.add(item);
                            }
                        }

                        if(selectedDateClasses.size() == 0){
                            emptyClass.setVisibility(View.VISIBLE);
                            scheduleList.setVisibility(View.GONE);
                        }
                        else {
                            emptyClass.setVisibility(View.GONE);
                            scheduleList.setVisibility(View.VISIBLE);
                            //set recycler view adapter
                            ScheduleAdapter adapter = new ScheduleAdapter(getContext(), selectedDateClasses);
                            scheduleList.setAdapter(adapter);
                        }
                    }

                    @Override
                    public void onCalendarScroll(HorizontalCalendarView calendarView, int dx, int dy) {

                    }
                });

                //set recycler view layout manager
                scheduleList.setLayoutManager(new LinearLayoutManager(getContext()));

                firstTimeLayout();

                horizontalCalendar.refresh();
            }
        });
    }

    public void firstTimeLayout(){
        //get today date object
        Date date = Calendar.getInstance().getTime();
        selectedDate.setText(DateUtility.convertToDateFormat(date));

        //Searching the classes at selected date
        ArrayList<ScheduleModel> selectedDateClasses = new ArrayList<>();
        for (int i = 0; i < allClassSchedule.size(); i++) {
            try {
                Date date2 = DateUtility.convertStringToDate(allClassSchedule.get(i).getClassDate());
                if (date2 != null && date2.equals(date)) {
                    ScheduleModel item = allClassSchedule.get(i);
                    selectedDateClasses.add(item);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        if(selectedDateClasses.size() == 0){
            emptyClass.setVisibility(View.VISIBLE);
            scheduleList.setVisibility(View.GONE);
        }
        else {
            emptyClass.setVisibility(View.GONE);
            scheduleList.setVisibility(View.VISIBLE);
            //set recycler view adapter
            ScheduleAdapter adapter = new ScheduleAdapter(getContext(), selectedDateClasses);
            scheduleList.setAdapter(adapter);
        }
    }

    private void setTextSize(){
        //multiple of text size
        float textSize = userPreferences.getTextSize();
        //set text size for each text view
        selectedDate.setTextSize(TypedValue.COMPLEX_UNIT_PX,selectedDate.getTextSize()*textSize);
        noClass.setTextSize(TypedValue.COMPLEX_UNIT_PX,noClass.getTextSize()*textSize);
    }

    private void setTextTypeface(){
        //get font type
        Typeface textTypeface = userPreferences.getTextTypeface();
        //set font type for each text view
        selectedDate.setTypeface(textTypeface, selectedDate.getTypeface().getStyle());
        noClass.setTypeface(textTypeface, noClass.getTypeface().getStyle());
    }
}
