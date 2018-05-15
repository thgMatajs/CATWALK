package com.thgmobi.catwalk.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.thgmobi.catwalk.MainActivity;
import com.thgmobi.catwalk.R;
import com.thgmobi.catwalk.util.Common;
import com.thgmobi.catwalk.util.VerificaCampo;

import java.nio.channels.Channels;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private TextInputLayout tiEmail;
    private TextInputLayout tiSenha;

    private TextInputEditText edtEmail;
    private TextInputEditText edtSenha;

    private Button btnEntrar;
    private Button btnCadastrar;
    private SignInButton btnGoogle;

    private String email = "";
    private String senha = "";


    private VerificaCampo vc = new VerificaCampo();

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private GoogleApiClient googleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initVars();
        initActions();
        startGoogleClient();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                }
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    private void initVars() {

        tiEmail = findViewById(R.id.ti_email);
        tiSenha = findViewById(R.id.ti_password);

        edtEmail = findViewById(R.id.login_edt_email);
        edtSenha = findViewById(R.id.login_edt_password);

        btnEntrar = findViewById(R.id.login_btn_entrar);
        btnCadastrar = findViewById(R.id.login_btn_cadastar);
        btnGoogle = findViewById(R.id.login_btn_google);

    }

    private void initActions() {
        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg = getString(R.string.campo_obrigatorio);

                email = edtEmail.getText().toString();
                senha = edtSenha.getText().toString();

                if (!vc.verificaCampoVazioComErro(tiEmail,edtEmail, msg)
                       && !vc.verificaCampoVazioComErro(tiSenha, edtSenha, msg)){

                    validarEmailSenha();

                }



            }
        });

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, CadastrarActivity.class));
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, Common.GOOGLE_SIGNIN_CODE);

            }
        });
    }

    private void validarEmailSenha(){
        firebaseAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }else {
                    Snackbar.make(btnEntrar, "E-Mail ou senha invalidos, tente novamente",
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }


    private void startGoogleClient() {

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.GOOGLE_SIGNIN_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {

           firebaseAuthWithGoogle(result.getSignInAccount());

        } else {
            Toast.makeText(this, "Não foi possivel efetuar o LogIn. Tente novamente", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount signInAccount) {

        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Não foi possivel se conectar. Tente novamente.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (firebaseAuthListener != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }
}
