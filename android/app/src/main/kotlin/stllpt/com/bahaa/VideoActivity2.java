package stllpt.com.bahaa;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.flutter.plugin.common.EventChannel;
import io.flutter.view.TextureRegistry;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import stllpt.com.flutchat.R;

public class VideoActivity2 extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, Session.SessionListener, PublisherKit.PublisherListener, SubscriberKit.SubscriberListener {

    // Suppressing this warning. mWebServiceCoordinator will get GarbageCollected if it is local.
    //private var mWebServiceCoordinator: WebServiceCoordinator? = null

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private Button btnEndCall;
    private TextView waitingTv;

    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;

    private final int RC_SETTINGS_SCREEN_PERM = 123;
    private final int RC_VIDEO_APP_PERM = 124;
    private final OpenTokUtils openTokUtils = new OpenTokUtils();

    private Surface surface;
    private TextureRegistry.SurfaceTextureEntry textureEntry;
    private EventChannel eventChannel;
    private QueuingEventSink eventSink = new QueuingEventSink();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        mPublisherViewContainer = findViewById(R.id.publisher_container);
        mSubscriberViewContainer = findViewById(R.id.subscriber_container);
        btnEndCall = findViewById(R.id.btnEndCall);
        waitingTv = findViewById(R.id.tvWaiting);

        eventChannel.setStreamHandler(new EventChannel.StreamHandler() {
            @Override
            public void onListen(Object arguments, EventChannel.EventSink events) {
                eventSink.setDelegate(events);
            }

            @Override
            public void onCancel(Object arguments) {
                eventSink.setDelegate(null);
            }
        });

        surface = new Surface(textureEntry.surfaceTexture());

        btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSession != null) {
                    Date date = mSession.getConnection().getCreationTime();
                    Log.i("SessionCall", "Start time is : " + date.getTime());
                    mSession.disconnect();
                    Intent intent = new Intent();
                    intent.putExtra("callStartTime", date);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });

        //requestPermissions();
        initializeSession(openTokUtils.API_KEY, openTokUtils.SESSION_ID, openTokUtils.TOKEN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Statuss", "onPause");

        if (mSession != null) {
            mSession.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i("Statuss", "onResume");

        if (mSession != null) {
            mSession.onResume();
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.d("Statuss", "onStreamCreated: Publisher Stream Created. Own stream " + stream.getStreamId());

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.d("Statuss", "onStreamDestroyed: Publisher Stream Destroyed. Own stream " + stream.getStreamId());

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    @Override
    public void onConnected(Session session) {
        Log.d("Statuss", "onConnected: Connected to session: " + session.getSessionId());

        // initialize Publisher and set this object to listen to Publisher events
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);

        MediaPlayer mediaPlayer = new MediaPlayer();
        //mediaPlayer.setSurface(mPublisher.getRenderer());

        // set publisher video style to fill view
        mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        mSubscriberViewContainer.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) (mPublisher.getView())).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);

    }

    @Override
    public void onDisconnected(Session session) {
        Log.d("Statuss", "onDisconnected: Disconnected from session: " + session.getSessionId());
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.d("Statuss", "onStreamReceived: New Stream Received " + stream.getStreamId() + " in session: " + session.getSessionId());
        waitingTv.setVisibility(View.GONE);

        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
            mSubscriber.setSubscriberListener(this);
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.d("Statuss", "onStreamDropped: Stream Dropped: " + stream.getStreamId() + " in session: " + session.getSessionId());

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }


    @Override
    public void onConnected(SubscriberKit subscriberKit) {
        Log.d("Statuss", "onConnected: Subscriber connected. Stream: " + subscriberKit.getStream().getStreamId());
    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {
        Log.d("Statuss", "onDisconnected: Subscriber disconnected. Stream: " + subscriberKit.getStream().getStreamId());
    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {

    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d("Statuss", "onPermissionsGranted:" + requestCode + ":" + perms.size());

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionDenied(this, String.valueOf(perms))) {
            new AppSettingsDialog.Builder(this)
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setRationale(getString(R.string.rationale_ask_again))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel))
                    .setRequestCode(RC_SETTINGS_SCREEN_PERM)
                    .build()
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions(){
        List<String> permissions = Arrays.asList(Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO);
        if (EasyPermissions.hasPermissions(this, String.valueOf(permissions))){
            initializeSession(openTokUtils.API_KEY, openTokUtils.SESSION_ID, openTokUtils.TOKEN);
        }
    }

    private void initializeSession(String apiKey, String sessionId, String token) {
        mSession = new Session.Builder(this, apiKey, sessionId).build();
        mSession.setSessionListener(this);
        mSession.connect(token);
    }
}