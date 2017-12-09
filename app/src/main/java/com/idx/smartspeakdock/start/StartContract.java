package com.idx.smartspeakdock.start;

import com.idx.smartspeakdock.BasePresenter;
import com.idx.smartspeakdock.BaseView;

/**
 * Created by derik on 17-12-9.
 * Email: lionel.lp.wu@mail.foxconn.com
 */

public interface StartContract {
    interface View extends BaseView<Presenter> {
        void showContent(String str);
        boolean isActive();
    }

    interface Presenter extends BasePresenter {
        void updateContent();
    }
}
