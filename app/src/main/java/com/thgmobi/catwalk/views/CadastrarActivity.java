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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.thgmobi.catwalk.R;
import com.thgmobi.catwalk.models.User;
import com.thgmobi.catwalk.util.VerificaCampo;

public class CadastrarActivity extends AppCompatActivity {

    private TextInputLayout tiNome;
    private TextInputLayout tiEmail;
    private TextInputLayout tiSenha;

    private TextInputEditText edtNome;
    private TextInputEditText edtEmail;
    private TextInputEditText edtSenha;

    private Button btnSalvar;
    private Button btnCancelar;

    private ProgressBar progressBar;


    private User user = new User();
    private VerificaCampo vc = new VerificaCampo();

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar);

        initVars();
        initActions();
    }

    private void initVars() {

        tiNome = findViewById(R.id.cadastro_ti_nome);
        tiEmail = findViewById(R.id.cadastro_ti_email);
        tiSenha = findViewById(R.id.cadastro_ti_senha);

        edtNome = findViewById(R.id.cadastro_edt_nome);
        edtEmail = findViewById(R.id.cadastro_edt_email);
        edtSenha = findViewById(R.id.cadastro_edt_senha);

        btnSalvar = findViewById(R.id.cadastro_btn_salvar);
        btnCancelar = findViewById(R.id.cadastro_btn_cancelar);

        progressBar = findViewById(R.id.cadastro_progressbar);

    }

    private void initActions() {

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!vc.verificaCampoVazioComErro(tiNome, edtNome, getString(R.string.campo_obrigatorio))
                        && !vc.verificaCampoVazioComErro(tiEmail, edtEmail, getString(R.string.campo_obrigatorio))
                        && !vc.verificaCampoVazioComErro(tiSenha, edtSenha, getString(R.string.campo_obrigatorio))){

                    user.setNome(edtNome.getText().toString());
                    user.setEmail(edtEmail.getText().toString());
                    user.setSenha(edtSenha.getText().toString());
                    btnSalvar.setText("");
                    progressBar.setVisibility(View.VISIBLE);
                    cadastraUser();

                }else{
                    btnSalvar.setText(getString(R.string.salvar));
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CadastrarActivity.this, LoginActivity.class));
                finish();
            }
        });



    }

    private void cadastraUser() {
        firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), user.getSenha()).addOnCompleteListener(CadastrarActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            user.setId(task.getResult().getUser().getUid());
                            user.salvaUserFirebase();

                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


                            String userId = user.getId();
                            if (userId != null){
                                finish();
                                firebaseAuth.signOut();
                            }

                            Toast.makeText(CadastrarActivity.this, "Cadastro efetuado com sucesso.", Toast.LENGTH_SHORT).show();
//                            finish();
                        }else {
                            btnSalvar.setText(getString(R.string.salvar));
                            progressBar.setVisibility(View.GONE);
                            tiEmail.setErrorEnabled(true);
                            tiSenha.setErrorEnabled(true);
                            String msgError;

                            try {
                                throw (task.getException());
                            }catch (FirebaseAuthWeakPasswordException e) {
                                msgError = "Senha fraca, tente outra com no mínimo 8 digitos.";
                                tiSenha.setError(msgError);
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                msgError = "E-mail invalido, verifique se digitou corretamente.";
                                tiEmail.setError(msgError);
                            } catch (FirebaseAuthUserCollisionException e) {
                                msgError = "E-mail ja está em uso, tente um novo.";
                                tiEmail.setError(msgError);
                            } catch (Exception e) {
                                msgError = "Erro ao cadastrar, tente novamente";
                                Snackbar.make(btnSalvar, msgError, Snackbar.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }
}
