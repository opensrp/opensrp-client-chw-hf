package org.smartregister.chw.hf.provider;

import android.content.Context;
import android.view.View;

import org.smartregister.provider.PmtctRegisterProvider;

import java.util.Set;

public class HeiRegisterProvider extends PmtctRegisterProvider {
    public HeiRegisterProvider(Context context, View.OnClickListener paginationClickListener, View.OnClickListener onClickListener, Set visibleColumns) {
        super(context, paginationClickListener, onClickListener, visibleColumns);
    }
}
