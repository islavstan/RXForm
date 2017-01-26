package com.islavstan.rxforms;

import android.graphics.Color;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.vicmikhailau.maskededittext.MaskedEditText;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {
    TextInputLayout emailInputLayout;
    TextInputLayout passwordInputLayout;
    TextInputLayout phoneInputLayout;
    TextInputLayout confirmInputLayout;
    EditText emailET;
    EditText confirmPasET;
    EditText passwordET;
    private Pattern pattern = android.util.Patterns.EMAIL_ADDRESS;
    private Matcher matcher;
    Button btn;
    MaskedEditText numberET;
//https://github.com/VicMikhailau/MaskedEditText

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailInputLayout = (TextInputLayout) findViewById(R.id.profile_input_email);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.profile_input_name);
        phoneInputLayout = (TextInputLayout) findViewById(R.id.profile_input_number);
        confirmInputLayout = (TextInputLayout) findViewById(R.id.profile_input_password2);
        emailET = (EditText) findViewById(R.id.profile_et_email);
        confirmPasET = (EditText) findViewById(R.id.profile_et_password2);
        passwordET = (EditText) findViewById(R.id.profile_et_name);
        btn = (Button) findViewById(R.id.profile_btn_submit);
        numberET = (MaskedEditText) findViewById(R.id.profile_et_number);

        btn.setOnClickListener(click -> Toast.makeText(this, "btn press", Toast.LENGTH_SHORT).show());


        Observable<CharSequence> emailChangeObservable = RxTextView.textChanges(emailET);
        Observable<CharSequence> passwordChangeObservable = RxTextView.textChanges(passwordET);
        // Observable<CharSequence> phoneChangeObservable = RxTextView.textChanges(numberET);
        Observable<CharSequence> confirmPassChangeObservable = RxTextView.textChanges(confirmPasET);

       /* Subscription numberSubscrioption = phoneChangeObservable.doOnNext(next -> hideError(3))
                .debounce(400, TimeUnit.MILLISECONDS)
                .filter(charSequence -> !TextUtils.isEmpty(charSequence))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CharSequence>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        if (charSequence.toString().length() < 17) {
                            showNumberLengthError();
                        } else {
                            hideError(2);
                        }
                    }
                });*/


        Subscription confirmPasswordSubscrioption = confirmPassChangeObservable.doOnNext(next -> hideError(4))
                .debounce(400, TimeUnit.MILLISECONDS)
                .filter(charSequence -> !TextUtils.isEmpty(charSequence))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CharSequence>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        boolean isPasswordValid = validatePassword(charSequence.toString());
                        if (charSequence.toString().length() < 6) {
                            showPasswordLengthError(2);
                        } else if (!isPasswordValid) {
                            showPasswordError(2);
                        } else if (!charSequence.toString().equals(passwordET.getText().toString())) {
                            showConfirmPasswordError();
                        } else {
                            hideError(4);
                        }
                    }
                });


        Subscription passwordSubscrioption = passwordChangeObservable.doOnNext(next -> hideError(2))
                .debounce(400, TimeUnit.MILLISECONDS)
                .filter(charSequence -> !TextUtils.isEmpty(charSequence))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CharSequence>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        boolean isPasswordValid = validatePassword(charSequence.toString());
                        if (charSequence.toString().length() < 6) {
                            showPasswordLengthError(1);
                        } else if (!isPasswordValid) {
                            showPasswordError(1);
                        } else {
                            hideError(2);
                        }
                    }
                });


        Subscription emailSubscription = emailChangeObservable
                .doOnNext(next -> hideError(1)) //применяется к результату
                .debounce(400, TimeUnit.MILLISECONDS) //выделяет элемент если конкретный TimeSpan прошел
                .filter(charSequence -> !TextUtils.isEmpty(charSequence))//текстовое поле не должно быть пустым
                .observeOn(AndroidSchedulers.mainThread()) // UI Thread
                .subscribe(new Subscriber<CharSequence>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(CharSequence charSequence) {
                        boolean isEmailValid = validateEmail(charSequence.toString());
                        if (!isEmailValid) {
                            showEmailError();
                        } else {
                            hideError(1);
                        }
                    }
                });

        Subscription signInFieldsSubscription = Observable.combineLatest(emailChangeObservable, passwordChangeObservable, confirmPassChangeObservable, new Func3<CharSequence, CharSequence, CharSequence, Boolean>() {
            @Override
            public Boolean call(CharSequence email, CharSequence password, CharSequence confPass) {
                boolean isEmailValid = validateEmail(email.toString());
                boolean isPasswordLengthValid = password.toString().length() >= 6;
                boolean isPasswordValid = validatePassword(password.toString());
                boolean isConfirmPasswordLengthValid = confPass.toString().length() >= 6;
                boolean isConfirmPasswordValid = validatePassword(confPass.toString());
                boolean isSamePass = password.toString().equals(confPass.toString());


                return isEmailValid && isPasswordLengthValid && isPasswordValid && isConfirmPasswordLengthValid && isConfirmPasswordValid && isSamePass;
            }
        }).observeOn(AndroidSchedulers.mainThread()) // UI Thread
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Boolean validFields) {
                        if (validFields) {
                            enableSignIn();
                        } else {
                            disableSignIn();
                        }
                    }


                });


        CompositeSubscription compositeSubscription = new CompositeSubscription();//он собирает подписчиков, потом их можно уничтожить в OnDestroy
        compositeSubscription.add(emailSubscription);


    }

    private void disableSignIn() {
        btn.setEnabled(false);
    }

    private void enableSignIn() {
        btn.setEnabled(true);
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email))
            return false;

        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password))
            return false;
        final Pattern pattern = Pattern.compile("^(?=.{6,12}$)(?=(.*[A-Z]){2})(?=(.*[a-z]){0,})(?=(.*[0-9]){2})(?=\\S+$).*$");
        matcher = pattern.matcher(password);
        return matcher.matches();
    }


    private void showEmailError() {

        emailInputLayout.setError("Invalid email");
    }


    private void showPasswordError(int layout) {
        switch (layout) {
            case 1:
                passwordInputLayout.setError("Password is weak. Please use at least two digits and two capitalized letters");
                break;
            case 2:
                confirmInputLayout.setError("Password is weak. Please use at least two digits and two capitalized letters");
                break;

        }

    }

    private void showPasswordLengthError(int layout) {
        switch (layout) {
            case 1:
                passwordInputLayout.setError("Password length should be 6-12 symbols");
                break;
            case 2:
                confirmInputLayout.setError("Password length should be 6-12 symbols");
                break;
        }

    }

    private void showNumberLengthError() {
        phoneInputLayout.setError("Number length should be 12 symbols");
    }

    private void showConfirmPasswordError() {
        confirmInputLayout.setError("Password is not the same");
    }

    private void hideError(int layout) {
        switch (layout) {
            case 1:
                emailInputLayout.setError(null);
                break;
            case 2:
                passwordInputLayout.setError(null);
                break;
            case 3:
                phoneInputLayout.setError(null);
                break;
            case 4:
                confirmInputLayout.setError(null);
                break;
        }

    }


}
