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

import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class MainActivity extends AppCompatActivity {
    TextInputLayout emailInputLayout;
    TextInputLayout passwordInputLayout;
    EditText emailET;
    EditText passwordET;
    private Pattern pattern = android.util.Patterns.EMAIL_ADDRESS;
    private Matcher matcher;
    Button btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailInputLayout = (TextInputLayout) findViewById(R.id.profile_input_email);
        passwordInputLayout = (TextInputLayout) findViewById(R.id.profile_input_name);
        emailET = (EditText) findViewById(R.id.profile_et_email);
        passwordET = (EditText) findViewById(R.id.profile_et_name);
        btn = (Button) findViewById(R.id.profile_btn_submit);

        // btn.setOnClickListener(click-> Log.d(LOG,"button press"));


        Observable<CharSequence> emailChangeObservable = RxTextView.textChanges(emailET);
        Observable<CharSequence> passwordChangeObservable = RxTextView.textChanges(passwordET);

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
                            showPasswordLengthError();
                        } else if (!isPasswordValid) {
                            showPasswordError();
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

        CompositeSubscription compositeSubscription = new CompositeSubscription();//он собирает подписчиков, потом их можно уничтожить в OnDestroy
        compositeSubscription.add(emailSubscription);


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
        final Pattern pattern = Pattern.compile
                ("(?=^.{6,12}$)((?=.*\\d{2,})|(?=.*\\W+))(?![.\\n])(?=.*[A-Z]{2,})(?=.*[a-z]).*$");
        matcher = pattern.matcher(password);
        return matcher.matches();
    }


    private void showEmailError() {

        emailInputLayout.setError("Invalid email");
    }


    private void showPasswordError() {
        passwordInputLayout.setError("Password is weak. Please use at least two digits and two capitalized letters");
    }

    private void showPasswordLengthError() {
        passwordInputLayout.setError("Password length should be 6-12 symbols");
    }


    private void hideError(int layout) {
        switch (layout) {
            case 1:
                emailInputLayout.setError(null);
                break;
            case 2:
                passwordInputLayout.setError(null);
                break;
        }

    }


}
