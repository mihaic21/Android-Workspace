package com.shobo.tipcalculator.app;

import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class TipCalculator extends ActionBarActivity {

    private static final String TOTAL_BILL = "TOTAL_BILL";
    private static final String CURRENT_TIP = "CURRENT_TIP";
    private static final String BILL_WITHOUT_TIP = "BILL_WITHOUT_TIP";

    private double billBeforeTip;
    private double tipAmount;
    private double finalBill;

    EditText billBeforeTipET;
    EditText tipAmountET;
    EditText finalBillET;

    SeekBar tipSeekBar;

    private int[] checkListValues = new int[12];
    
    CheckBox friendlyCheckBox;
    CheckBox specialsCheckBox;
    CheckBox opinionCheckBox;
    
    RadioGroup availableRadioGroup;
    RadioButton availableBadRadio;
    RadioButton availableOKRadio;
    RadioButton availableGoodRadio;
    
    Spinner problemsSpinner;
    
    Button startChronometerButton;
    Button pauseChronometerButton;
    Button resetChronometerButton;
    
    Chronometer timeWaitingChronometer;
    
    long secondsYouWaited = 0;
    
    TextView timeWaitingTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tip_calculator);

        if (savedInstanceState == null){

            billBeforeTip = 0.0;
            tipAmount = .15;
            finalBill = 0.0;

        } else {

            billBeforeTip = savedInstanceState.getDouble(BILL_WITHOUT_TIP);
            tipAmount = savedInstanceState.getDouble(CURRENT_TIP);
            finalBill = savedInstanceState.getDouble(TOTAL_BILL);

        }

        billBeforeTipET = (EditText) findViewById(R.id.billEditText);
        tipAmountET = (EditText) findViewById(R.id.tipEditText);
        finalBillET = (EditText) findViewById(R.id.finalBillEditText);

        tipSeekBar = (SeekBar) findViewById(R.id.changeTipSeekBar);

        tipSeekBar.setOnSeekBarChangeListener(tipSeekBarListener);

        billBeforeTipET.addTextChangedListener(billBeforeTipListener);
        
        friendlyCheckBox = (CheckBox) findViewById(R.id.friendlyCheckBox);
        specialsCheckBox = (CheckBox) findViewById(R.id.specialsCheckBox);
        opinionCheckBox = (CheckBox) findViewById(R.id.opinionCheckBox);
        
        setUpIntroCheckBoxes();
        
        availableRadioGroup = (RadioGroup) findViewById(R.id.availableRadioGroup);
        availableBadRadio = (RadioButton) findViewById(R.id.availableBadRadio);
        availableOKRadio = (RadioButton) findViewById(R.id.availableOKRadio);
        availableGoodRadio = (RadioButton) findViewById(R.id.availableGoodRadio);
        
        addChangeListenerToRadios();
        
        problemsSpinner = (Spinner) findViewById(R.id.problemsSpinner);
        
        addItemSelectedListenerToSpinner();
        
        startChronometerButton = (Button) findViewById(R.id.startChronometerButton);
        pauseChronometerButton = (Button) findViewById(R.id.pauseChronometerButton);
        resetChronometerButton = (Button) findViewById(R.id.resetChronometerButton);
        
        setButtonClickListeners();
        
        timeWaitingChronometer = (Chronometer) findViewById(R.id.timeWaitingChronometer);
        
        timeWaitingTextView = (TextView) findViewById(R.id.timeWaitingTextView);

    }

    private void setButtonClickListeners() {

        startChronometerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int stoppedMilliseconds = 0;

                String chronoText = timeWaitingChronometer.getText().toString();
                String array[] = chronoText.split(":");

                if (array.length == 2){
                    stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 1000 +
                            Integer.parseInt(array[1]) * 1000;
                } else if (array.length == 3){
                    stoppedMilliseconds = Integer.parseInt(array[0]) * 60 * 60 * 1000 +
                            Integer.parseInt(array[1]) * 60 * 1000 +
                            Integer.parseInt(array[2]) * 1000;
                }

                timeWaitingChronometer.setBase(SystemClock.elapsedRealtime() - stoppedMilliseconds);

                secondsYouWaited = Long.parseLong(array[1]);

                updateTipBasedOnTimeWaited(secondsYouWaited);

                timeWaitingChronometer.start();

            }
        });

        pauseChronometerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                timeWaitingChronometer.stop();

            }
        });

        resetChronometerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                timeWaitingChronometer.setBase(SystemClock.elapsedRealtime());

                secondsYouWaited = 0;

            }
        });

    }

    private void updateTipBasedOnTimeWaited(long secondsYouWaited) {

        checkListValues[9] = (secondsYouWaited > 10)?-2:2;

        setTipFromWaitressCheckList();

        updateTipAndFinalBill();

    }

    private void addItemSelectedListenerToSpinner() {

        problemsSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                checkListValues[6] = (problemsSpinner.getSelectedItem().equals("Bad"))?-1:0;
                checkListValues[7] = (problemsSpinner.getSelectedItem().equals("OK"))?3:0;
                checkListValues[8] = (problemsSpinner.getSelectedItem().equals("Good"))?6:0;

                setTipFromWaitressCheckList();

                updateTipAndFinalBill();

            }
        });

    }

    private void addChangeListenerToRadios() {

        availableRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {

                checkListValues[3] = (availableBadRadio.isChecked())?-1:0;
                checkListValues[4] = (availableOKRadio.isChecked())?2:0;
                checkListValues[5] = (availableGoodRadio.isChecked())?4:0;

                setTipFromWaitressCheckList();

                updateTipAndFinalBill();

            }
        });

    }

    private void setUpIntroCheckBoxes() {

        friendlyCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                checkListValues[0] = (friendlyCheckBox.isChecked())?4:0;

                setTipFromWaitressCheckList();

                updateTipAndFinalBill();

            }
        });

        specialsCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                checkListValues[1] = (specialsCheckBox.isChecked())?1:0;

                setTipFromWaitressCheckList();

                updateTipAndFinalBill();

            }
        });

        opinionCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                checkListValues[2] = (opinionCheckBox.isChecked())?2:0;

                setTipFromWaitressCheckList();

                updateTipAndFinalBill();

            }
        });

    }

    private void setTipFromWaitressCheckList() {

        int checkListTotal = 0;

        for (int item : checkListValues){

            checkListTotal += item;

        }

        tipAmountET.setText(String.format("%.02f", checkListTotal + 0.1));

    }

    private TextWatcher billBeforeTipListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            try{

                billBeforeTip = Double.parseDouble(charSequence.toString());

            }catch (NumberFormatException e){
                billBeforeTip = 0.0;
            }

            updateTipAndFinalBill();

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void updateTipAndFinalBill() {

        double tipAmount = Double.parseDouble(tipAmountET.getText().toString());

        double finalBill = billBeforeTip + (billBeforeTip * tipAmount);

        finalBillET.setText(String.format("%.02f",finalBill));

    }

    protected void onSaveInstanceState(Bundle outState){

        super.onSaveInstanceState(outState);

        outState.putDouble(TOTAL_BILL, finalBill);
        outState.putDouble(CURRENT_TIP, tipAmount);
        outState.putDouble(BILL_WITHOUT_TIP, billBeforeTip);

    }

    private SeekBar.OnSeekBarChangeListener tipSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            tipAmount = (tipSeekBar.getProgress()) * .01;

            tipAmountET.setText(String.format("%.02f", tipAmount));

            updateTipAndFinalBill();

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tip_calculator, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
