package Unlike.tabatmie.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.util.HashMap;

import Unlike.tabatmie.BuildConfig;
import Unlike.tabatmie.R;
import Unlike.tabatmie.connect.CallRetrofit;
import Unlike.tabatmie.util.Applications;
import Unlike.tabatmie.util.CommonUtil;
import Unlike.tabatmie.util.Preference;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class SettingActivity extends AppCompatActivity {

    private String TAG = this.getClass().toString();

    @BindView(R.id.btn_back)
    RelativeLayout btn_back;

    @BindView(R.id.btn_sound_on)
    RelativeLayout btn_sound_on;
    @BindView(R.id.tv_sound_on)
    TextView tv_sound_on;
    @BindView(R.id.btn_sound_off)
    RelativeLayout btn_sound_off;
    @BindView(R.id.tv_sound_off)
    TextView tv_sound_off;

    @BindView(R.id.switch_pause)
    Switch switch_pause;

    @BindView(R.id.tv_version)
    TextView tv_version;

    @BindView(R.id.btn_terms)
    TextView btn_terms;

    @BindView(R.id.btn_logout)
    TextView btn_logout;
    @BindView(R.id.btn_login)
    RelativeLayout btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ButterKnife.bind(this);

        try {
            Applications.setRefreshActivity(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        init();
    }

    public void init() {
        setSound(Applications.preference.getValue(Preference.SOUND, CommonUtil.D_SOUND));

        switch_pause.setChecked(Applications.preference.getValue(Preference.EXERCISE_PAUSE, CommonUtil.D_PAUSE));

        tv_version.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        String token = Applications.preference.getValue(Preference.TOKEN, "");
        if (token.isEmpty()) {
            btn_logout.setVisibility(View.GONE);
            btn_login.setVisibility(View.VISIBLE);
        } else {
            btn_logout.setVisibility(View.VISIBLE);
            btn_login.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.btn_back, R.id.btn_sound_on, R.id.btn_sound_off, R.id.switch_pause, R.id.btn_terms, R.id.btn_logout, R.id.btn_login})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                onBackPressed();
                break;
            case R.id.btn_sound_on:
                setSound(true);
                break;
            case R.id.btn_sound_off:
                setSound(false);
                break;
            case R.id.switch_pause:
                Applications.preference.put(Preference.EXERCISE_PAUSE, switch_pause.isChecked());
                break;
            case R.id.btn_terms:
                goTerms();
                break;
            case R.id.btn_logout:
                btn_logout.setVisibility(View.GONE);
                btn_login.setVisibility(View.VISIBLE);
                CommonUtil.logout();
                break;
            case R.id.btn_login:
                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(SettingActivity.this)) {
                    UserApiClient.getInstance().loginWithKakaoTalk(SettingActivity.this, kLoginCallback);
                } else {
                    UserApiClient.getInstance().loginWithKakaoAccount(SettingActivity.this, kLoginCallback);
                }
                break;
        }
    }

    public void setSound(boolean sound) {
        Applications.preference.put(Preference.SOUND, sound);
        if (sound) {
            btn_sound_on.setBackground(ContextCompat.getDrawable(this, R.drawable.layer_main_round_21));
            btn_sound_off.setBackground(null);
            tv_sound_on.setTextColor(ContextCompat.getColor(this, R.color.white));
            tv_sound_off.setTextColor(ContextCompat.getColor(this, R.color.defalut_black));
        } else {
            btn_sound_on.setBackground(null);
            btn_sound_off.setBackground(ContextCompat.getDrawable(this, R.drawable.layer_main_round_21));
            tv_sound_on.setTextColor(ContextCompat.getColor(this, R.color.defalut_black));
            tv_sound_off.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    Function2<OAuthToken, Throwable, Unit> kLoginCallback = new Function2<OAuthToken, Throwable, Unit>() {
        @Override
        public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
            if (throwable != null) {
                Log.e(TAG, "Login fail" + throwable.toString());
                Toast.makeText(SettingActivity.this, "Login Fail", Toast.LENGTH_SHORT).show();
            } else {
                if (oAuthToken != null) {
                    UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
                        @Override
                        public Unit invoke(User user, Throwable throwable) {
                            if (user != null) {
                                if (!user.getKakaoAccount().getEmail().isEmpty() && user.getId() > 0) {
                                    String email = user.getKakaoAccount().getEmail();
                                    int id = (int) user.getId();
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("email", email);
                                    map.put("snsId", id);
                                    CallRetrofit call = new CallRetrofit();
                                    call.setHashMap(map);
                                    call.callLogin(false);
                                    Applications.preference.put(Preference.EMAIL, email);
                                }
                            } else {
                                Toast.makeText(SettingActivity.this, "Login Fail", Toast.LENGTH_SHORT).show();
                            }
                            return null;
                        }
                    });

                } else {
                    Toast.makeText(SettingActivity.this, "Login Fail", Toast.LENGTH_SHORT).show();
                }
            }
            return null;
        }
    };

    public void goTerms(){
        Intent goLogin = new Intent(SettingActivity.this, TermsActivity.class);
        startActivity(goLogin);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}