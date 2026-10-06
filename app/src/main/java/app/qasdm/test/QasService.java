package app.qasdm.test;

import android.app.*;
import android.content.*;
import android.os.*;
import android.media.*;
import android.media.session.*;
import android.graphics.Bitmap;
import android.content.pm.ServiceInfo;
import org.json.JSONObject;
import java.io.File;

/** User-enabled background role runtime and native media playback. */
public class QasService extends Service {
    static QasService current;
    private MediaPlayer player;
    private MediaSession session;
    private AudioManager audioManager;
    private AudioFocusRequest focus;
    private PowerManager.WakeLock wake;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String title="音乐房间",artist="QasDM";
    private Bitmap cover;
    private boolean roles,loading;
    private File trackFile;
    private final Runnable tick=new Runnable(){public void run(){
        if(roles) MainActivity.emit("window.qasBackgroundTick52&&window.qasBackgroundTick52()");
        if(player!=null&&!loading){try{publishState();MainActivity.emit("window.qasMediaState52&&window.qasMediaState52("+player.getCurrentPosition()+","+player.getDuration()+","+player.isPlaying()+")");}catch(IllegalStateException ignored){}}
        handler.postDelayed(this,1000);
    }};
    @Override public void onCreate(){super.onCreate();current=this;
        NotificationManager nm=getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("qas-runtime","后台运行",NotificationManager.IMPORTANCE_LOW));
        nm.createNotificationChannel(new NotificationChannel("qas-music","音乐房间",NotificationManager.IMPORTANCE_LOW));
        audioManager=(AudioManager)getSystemService(AUDIO_SERVICE);focus=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()).setOnAudioFocusChangeListener(change->{if(change<0)command("pause",0);}).build();
        session=new MediaSession(this,"QasDM Music");session.setCallback(new MediaSession.Callback(){
            @Override public void onPlay(){command("play",0);}
            @Override public void onPause(){command("pause",0);}
            @Override public void onSkipToNext(){MainActivity.emit("window.qasMediaCommand52&&window.qasMediaCommand52('next')");}
            @Override public void onSkipToPrevious(){MainActivity.emit("window.qasMediaCommand52&&window.qasMediaCommand52('previous')");}
            @Override public void onSeekTo(long pos){command("seek",pos);}
            @Override public void onStop(){stopMusic();}
        });
        wake=((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"QasDM:Runtime");
        handler.post(tick);
    }
    @Override public int onStartCommand(Intent i,int flags,int id){
        if(i!=null){String action=i.getAction();if("roles".equals(action)){roles=i.getBooleanExtra("enabled",false);getSharedPreferences("qas-native",0).edit().putBoolean("background",roles).apply();}
            else if("media".equals(action)){loading=true;}
            else if("pause".equals(action)||"play".equals(action))command(action,0);
            else if("next".equals(action)||"previous".equals(action))MainActivity.emit("window.qasMediaCommand52&&window.qasMediaCommand52('"+action+"')");
            else if("stop".equals(action)){roles=false;getSharedPreferences("qas-native",0).edit().putBoolean("background",false).apply();MainActivity.emit("window.qasBackgroundEnabled=false");stopMusic();}
        }
        refresh();return START_NOT_STICKY;
    }
    void load(File file,JSONObject data) throws Exception {
        if(player!=null){player.release();player=null;}if(trackFile!=null)trackFile.delete();trackFile=file;
        title=data.optString("title","音乐房间");artist=data.optString("artist","QasDM");cover=MainActivity.decodeImage(data.optString("cover"));
        player=new MediaPlayer();player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
        player.setDataSource(file.getAbsolutePath());player.setOnPreparedListener(p->{loading=false;if(audioManager.requestAudioFocus(focus)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED)p.start();refresh();MainActivity.emit("window.qasMediaReady52&&window.qasMediaReady52()");});
        player.setOnCompletionListener(p->{MainActivity.emit("window.qasMediaCommand52&&window.qasMediaCommand52('ended')");refresh();});
        player.setOnErrorListener((p,what,extra)->{MainActivity.emit("window.qasMediaError52&&window.qasMediaError52()");stopMusic();return true;});
        loading=true;player.prepareAsync();refresh();
    }
    void command(String cmd,long position){if(player==null)return;try{if("play".equals(cmd)&&audioManager.requestAudioFocus(focus)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED)player.start();if("pause".equals(cmd))player.pause();if("seek".equals(cmd))player.seekTo((int)position);refresh();MainActivity.emit("window.qasMediaCommand52&&window.qasMediaCommand52('"+cmd+"')");}catch(IllegalStateException ignored){}}
    private PendingIntent action(String cmd){return PendingIntent.getService(this,cmd.hashCode(),new Intent(this,QasService.class).setAction(cmd),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);}
    private PendingIntent open(){return PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);}
    private void publishState(){if(player==null||loading)return;try{session.setMetadata(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE,title).putString(MediaMetadata.METADATA_KEY_ARTIST,artist).putLong(MediaMetadata.METADATA_KEY_DURATION,player.getDuration()).putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART,cover).build());session.setPlaybackState(new PlaybackState.Builder().setActions(PlaybackState.ACTION_PLAY|PlaybackState.ACTION_PAUSE|PlaybackState.ACTION_SKIP_TO_NEXT|PlaybackState.ACTION_SKIP_TO_PREVIOUS|PlaybackState.ACTION_SEEK_TO|PlaybackState.ACTION_STOP).setState(player.isPlaying()?PlaybackState.STATE_PLAYING:PlaybackState.STATE_PAUSED,player.getCurrentPosition(),player.isPlaying()?1:0).build());}catch(IllegalStateException ignored){}}
    private void refresh(){if(!roles&&player==null&&!loading){stopForeground(STOP_FOREGROUND_REMOVE);stopSelf();return;}
        if(!wake.isHeld())wake.acquire(60*60*1000L);
        boolean music=player!=null||loading;session.setActive(music);
        Notification.Builder b=new Notification.Builder(this,music?"qas-music":"qas-runtime").setSmallIcon(R.drawable.ic_qasdm).setContentTitle(music?title:"QasDM 后台运行").setContentText(music?artist:"角色主动互动与日记已开启").setContentIntent(open()).setOngoing(true);
        if(music){publishState();boolean playing=false;try{playing=player.isPlaying();}catch(Exception ignored){}
            b.setLargeIcon(cover).addAction(new Notification.Action.Builder(android.R.drawable.ic_media_previous,"上一首",action("previous")).build()).addAction(new Notification.Action.Builder(playing?android.R.drawable.ic_media_pause:android.R.drawable.ic_media_play,playing?"暂停":"播放",action(playing?"pause":"play")).build()).addAction(new Notification.Action.Builder(android.R.drawable.ic_media_next,"下一首",action("next")).build()).setStyle(new Notification.MediaStyle().setMediaSession(session.getSessionToken()).setShowActionsInCompactView(0,1,2));
        }else b.addAction(new Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel,"停止后台运行",action("stop")).build());
        if(Build.VERSION.SDK_INT>=34)startForeground(52,b.build(),music?ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK:ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);else startForeground(52,b.build());
    }
    void stopMusic(){audioManager.abandonAudioFocusRequest(focus);loading=false;if(player!=null){player.release();player=null;}if(trackFile!=null){trackFile.delete();trackFile=null;}session.setActive(false);refresh();}
    @Override public IBinder onBind(Intent i){return null;}
    @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);if(player!=null)player.release();session.release();audioManager.abandonAudioFocusRequest(focus);if(wake.isHeld())wake.release();current=null;super.onDestroy();}
}
