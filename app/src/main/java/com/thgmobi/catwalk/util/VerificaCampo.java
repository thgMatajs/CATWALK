package com.thgmobi.catwalk.util;

import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;

public class VerificaCampo {

    public boolean verificaCampoVazioComErro(TextInputLayout textInputLayout,
                                             TextInputEditText textInputEditText, String msgErro){

        if (textInputEditText.getText().toString().isEmpty()){
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError(msgErro);
            return true;
        } else {
            textInputLayout.setErrorEnabled(false);
            return false;
        }

    }
}
