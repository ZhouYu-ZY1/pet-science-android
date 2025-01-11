package com.zhouyu.android_create.tools;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaScannerConnection;
import android.os.Environment;

import com.zhouyu.android_create.R;
import com.zhouyu.android_create.application.Application;

import java.io.File;
import java.nio.ByteBuffer;

//参考地址：https://blog.csdn.net/u010126792/article/details/86510903
public class MediaMuxerTool {
    private static MediaExtractor mediaExtractor;


   /* public static File videoToAudioFFmpeg(String videoSrc,String audioName) {
        try {
            File parentFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"/微音乐/");
            if(!parentFile.exists()){
                boolean mkdirs = parentFile.mkdirs();
                if(!mkdirs){
                    return null;
                }
            }

            File file = new File(parentFile, FileTool.getFileNameWithoutSuffix(audioName) + ".mp3");
            if (file.exists()){
                boolean delete = file.delete();
                if(!delete){
                    return null;
                }
            }
            String command = "-i " + videoSrc + " -vn -ar 44100 -ac 2 -b:a 192k -acodec libmp3lame " + file.getPath();
            ConsoleUtils.logErr(command);
            FFmpegSession session = FFmpegKit.execute(command);
            if(session.getReturnCode().isSuccess()){
                ConsoleUtils.logErr("成功");
            }else {
                ConsoleUtils.logErr("失败");
            }
            return file;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }*/


    /**
     * 视频转音频
     */

    public static File videoToAudio(String videoSrc, String audioName) {
        MediaExtractor mediaExtractor = null;
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                    Application.context.getString(R.string.app_name) + "/视频提取音频";
            File parentFile = new File(path);
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    return null;
                }
            }

            File file = new File(parentFile, FileTool.getFileNameWithoutSuffix(audioName) + ".m4a");
            if (file.exists() && !file.delete()) {
                return null;
            }

            if (file.createNewFile()) {
                MediaMuxer mMediaMuxer;
                int mAudioTrackIndex;
                mediaExtractor = new MediaExtractor(); // 分离视频文件的音轨和视频轨道
                mediaExtractor.setDataSource(videoSrc); // 设置媒体文件的位置

                for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                    MediaFormat format = mediaExtractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime != null && mime.startsWith("audio")) { // 获取音频轨道
                        ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
                        mediaExtractor.selectTrack(i); // 选择此音频轨道

                        // 初始化 MediaMuxer
                        mMediaMuxer = new MediaMuxer(file.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                        mAudioTrackIndex = mMediaMuxer.addTrack(format);
                        mMediaMuxer.start();

                        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                        int sampleSize;

                        while ((sampleSize = mediaExtractor.readSampleData(buffer, 0)) > 0) {
                            info.offset = 0;
                            info.size = sampleSize;
                            info.presentationTimeUs = mediaExtractor.getSampleTime(); // 使用实际的样本时间戳

                            // 检查 MediaExtractor 的 flags
                            int sampleFlags = mediaExtractor.getSampleFlags();
                            if ((sampleFlags & MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                                info.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME;
                            } else {
                                info.flags = 0; // 清除其他标志
                            }

                            mMediaMuxer.writeSampleData(mAudioTrackIndex, buffer, info);
                            mediaExtractor.advance();
                        }

                        mMediaMuxer.stop();
                        mMediaMuxer.release();
                    }
                }

                // 通知系统新文件生成
//                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                //将新创建或下载的媒体文件传递给媒体扫描服务
                MediaScannerConnection.scanFile(Application.context, new String[]{file.getAbsolutePath()}, null, null);
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaExtractor != null) {
                mediaExtractor.release();
            }
        }
        return null;
    }
    /*public static File videoToAudio(String videoSrc, String audioName) {
        MediaExtractor mediaExtractor = null;
        try {
            File parentFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/微音乐/");
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    return null;
                }
            }

            File file = new File(parentFile, FileTool.getFileNameWithoutSuffix(audioName) + ".m4a");
            if (file.exists() && !file.delete()) {
                return null;
            }

            if (file.createNewFile()) {
                MediaMuxer mMediaMuxer;
                int mAudioTrackIndex;
                mediaExtractor = new MediaExtractor(); // 分离视频文件的音轨和视频轨道
                mediaExtractor.setDataSource(videoSrc); // 设置媒体文件的位置

                for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                    MediaFormat format = mediaExtractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime != null && mime.startsWith("audio")) { // 获取音频轨道
                        ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
                        mediaExtractor.selectTrack(i); // 选择此音频轨道

                        // 初始化 MediaMuxer
                        mMediaMuxer = new MediaMuxer(file.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                        mAudioTrackIndex = mMediaMuxer.addTrack(format);
                        mMediaMuxer.start();

                        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                        int sampleSize;

                        while ((sampleSize = mediaExtractor.readSampleData(buffer, 0)) > 0) {
                            info.offset = 0;
                            info.size = sampleSize;
                            info.flags = mediaExtractor.getSampleFlags();
                            info.presentationTimeUs = mediaExtractor.getSampleTime(); // 使用实际的样本时间戳
                            mMediaMuxer.writeSampleData(mAudioTrackIndex, buffer, info);
                            mediaExtractor.advance();
                        }

                        mMediaMuxer.stop();
                        mMediaMuxer.release();
                    }
                }

                // 通知系统新文件生成
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaExtractor != null) {
                mediaExtractor.release();
            }
        }
        return null;
    }*/


/*    public static File videoToAudio(String videoSrc,String audioName) {
        try {
            File parentFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"/微音乐/");
            if(!parentFile.exists()){
                boolean mkdirs = parentFile.mkdirs();
                if(!mkdirs){
                    return null;
                }
            }

//            File file;
//            String name = file.getName();
            File file = new File(parentFile,FileTool.getFileNameWithoutSuffix(audioName)+".m4a");
            if (file.exists()){
                boolean delete = file.delete();
                if(!delete){
                    return null;
                }
            }
            boolean newFile = file.createNewFile();
            if(newFile){
                MediaMuxer mMediaMuxer;
                int mAudioTrackIndex;
                long frameRate;

                mediaExtractor = new MediaExtractor();//此类可分离视频文件的音轨和视频轨道
                mediaExtractor.setDataSource(videoSrc);//媒体文件的位置
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                String fileMime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
                System.out.println("==========getTrackCount()===================" + mediaExtractor.getTrackCount());
                for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
                    MediaFormat format = mediaExtractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime.startsWith("audio")) {//获取音频轨道
                        ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
                        {
                            mediaExtractor.selectTrack(i);//选择此音频轨道
                            mediaExtractor.readSampleData(buffer, 0);
                            long first_sampletime = mediaExtractor.getSampleTime();
                            mediaExtractor.advance();
                            long second_sampletime = mediaExtractor.getSampleTime();
                            frameRate = Math.abs(second_sampletime - first_sampletime);//时间戳
                            mediaExtractor.unselectTrack(i);
                        }
                        mediaExtractor.selectTrack(i);
                        System.out.println("==============frameRate111=============="+frameRate+"");
                        mMediaMuxer = new MediaMuxer(file.getPath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                        mAudioTrackIndex = mMediaMuxer.addTrack(format);
                        mMediaMuxer.start();

                        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                        info.presentationTimeUs = 0;


                        int sampleSize;
                        while ((sampleSize = mediaExtractor.readSampleData(buffer, 0)) > 0) {
                            info.offset = 0;
                            info.size = sampleSize;
                            info.flags = mediaExtractor.getSampleFlags();
                            info.presentationTimeUs += frameRate;
                            mMediaMuxer.writeSampleData(mAudioTrackIndex, buffer, info);
                            mediaExtractor.advance();
                        }

                        mMediaMuxer.stop();
                        mMediaMuxer.release();
                    }
                }
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(mediaExtractor != null){
                mediaExtractor.release();
                mediaExtractor = null;
            }
        }
        return null;
    }*/
}
