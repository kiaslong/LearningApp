package com.ppl.learningapp;


import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class LoginSignupActivity extends AppCompatActivity {

    EditText emailLogin;
    EditText passwordLogin;
    EditText dateofBirth;
    EditText registerMail;
    EditText registerPass;
    EditText passConfirm;

    Button loginButton;
    Button registerButton;
    TextView signupTextView;
    TextView loginTextView ;

    LinearLayout loginLayout;
    LinearLayout registrationLayout;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://learningapp-cc1b9-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference reference = database.getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        emailLogin = findViewById(R.id.emailLogin);
        passwordLogin = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);

        dateofBirth =findViewById(R.id.dateOfBirth);
        registerMail=findViewById(R.id.email);
        registerPass=findViewById(R.id.registerPassword);
        passConfirm=findViewById(R.id.confirmPassword);
        registerButton=findViewById(R.id.btnRegister);




        loginTextView = findViewById(R.id.tvLogin);


        signupTextView = findViewById(R.id.tvSignup);
        loginLayout = findViewById(R.id.loginLayout);
        registrationLayout = findViewById(R.id.register);

        SpannableString loginSpannable = new SpannableString("Already have an account? Login Now");
        SpannableString signupSpannable = new SpannableString("Not yet registered? Sign Up Now");

        registrationLayout.setVisibility(View.GONE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = emailLogin.getText().toString();
                final String password = passwordLogin.getText().toString();

                if (email.isEmpty()) {
                    emailLogin.setError("Email is required");
                    emailLogin.requestFocus();

                }  else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && !email.equals("admin")) {
                    emailLogin.setError("Enter a valid email address");
                    emailLogin.requestFocus();

                }else if (password.isEmpty() ) {
                    passwordLogin.setError("Password is required");
                    passwordLogin.requestFocus();
                }
                else if (password.length() < 6 && !password.equals("admin")) {
                    passwordLogin.setError("Password must be at least 6 characters");
                    passwordLogin.requestFocus();
                } else if (email.equals("admin") && password.equals("admin")) {
                    // Handle admin login
                    Toast.makeText(LoginSignupActivity.this, "Admin login successful", Toast.LENGTH_SHORT).show();
                } else {
                    // Perform database check for user login
                    DatabaseReference usersRef = reference.child("users");
                    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                    usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                boolean userAuthenticated = false;
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    String storedPassword = userSnapshot.child("password").getValue(String.class);

                                    if (storedPassword != null && passwordEncoder.matches(password, storedPassword)) {
                                        // Password matches, user is authenticated
                                        userAuthenticated = true;
                                        break;
                                    }
                                }

                                if (userAuthenticated) {
                                    // User is authenticated, handle successful login
                                    Toast.makeText(LoginSignupActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Password is incorrect, handle login failure
                                    Toast.makeText(LoginSignupActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Email not found in the database
                                Toast.makeText(LoginSignupActivity.this, "Email not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle database error
                            Toast.makeText(LoginSignupActivity.this, "Error checking email: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });



        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateFields()) {
                    final String dob = dateofBirth.getText().toString();
                    final String email = registerMail.getText().toString();
                    final String password = registerPass.getText().toString();
                    final String[] temp = email.split("@");
                    final String username = temp[0];

                    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                    String hashedPassword = passwordEncoder.encode(password);

                    // Reference to the "users" node in the database
                    DatabaseReference usersRef = reference.child("users");

                    // Check if the email already exists
                    usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Email already exists, show an error message
                                Toast.makeText(LoginSignupActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                                Log.d("data", "onDataChange:" +dataSnapshot.toString());
                            } else {

                                User user = new User(username, dob, email, hashedPassword);
                                usersRef.child(username).setValue(user);
                                Toast.makeText(LoginSignupActivity.this, "Successfully Registered", Toast.LENGTH_SHORT).show();

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle the email query error
                            Toast.makeText(LoginSignupActivity.this, "Error checking email: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });


// ClickableSpan for Login
        ClickableSpan loginClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                // Handle the Login click event
                // For example, show the login form
                loginLayout.setVisibility(View.VISIBLE);
                registrationLayout.setVisibility(View.GONE);
                loginTextView.setVisibility(View.GONE);
                signupTextView.setVisibility(View.VISIBLE);
                emailLogin.setText("");
                passwordLogin.setText("");
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false); // Remove underline from the clickable text
            }
        };

// ClickableSpan for Sign Up
        ClickableSpan signupClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                // Handle the Sign Up click event
                // For example, show the registration form
                loginLayout.setVisibility(View.GONE);
                registrationLayout.setVisibility(View.VISIBLE);
                loginTextView.setVisibility(View.VISIBLE);
                signupTextView.setVisibility(View.GONE);
                dateofBirth.setText("");
                registerMail.setText("");
                registerPass.setText("");
                passConfirm.setText("");
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false); // Remove underline from the clickable text
            }
        };

// Set ClickableSpan for Login
        loginSpannable.setSpan(loginClickableSpan, 25, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        loginTextView.setText(loginSpannable);
        loginTextView.setMovementMethod(LinkMovementMethod.getInstance());
        loginTextView.setHighlightColor(Color.TRANSPARENT);

// Set ClickableSpan for Sign Up
        signupSpannable.setSpan(signupClickableSpan, 20, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        signupTextView.setText(signupSpannable);
        signupTextView.setMovementMethod(LinkMovementMethod.getInstance());
        signupTextView.setHighlightColor(Color.TRANSPARENT);


    }

    private boolean validateFields() {
        String dob = dateofBirth.getText().toString();
        String email = registerMail.getText().toString();
        String password = registerPass.getText().toString();
        String confirmPassword = passConfirm.getText().toString();

        if (dob.isEmpty()) {
            dateofBirth.setError("Date of Birth is required");
            dateofBirth.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            registerMail.setError("Email is required");
            registerMail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            registerMail.setError("Enter a valid email address");
            registerMail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            registerPass.setError("Password is required");
            registerPass.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            registerPass.setError("Password must be at least 6 characters");
            registerPass.requestFocus();
            return false;
        }

        if (!confirmPassword.equals(password)) {
            passConfirm.setError("Passwords do not match");
            passConfirm.requestFocus();
            return false;
        }

        return true;
    }






}