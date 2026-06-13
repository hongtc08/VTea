package com.vtea.utils;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class SoundHelper {

    /**
     * Phát ra tiếng "Ting" (mô phỏng tiếng chuông nhỏ)
     */
    public static void playTingSound() {
        new Thread(() -> {
            try {
                // Định dạng âm thanh: 8000Hz, 8-bit, mono, signed, big-endian
                AudioFormat format = new AudioFormat(8000f, 8, 1, true, true);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();

                // Tạo dải sóng Sine (Tần số 880Hz - nốt A5) trong 0.15 giây
                int sampleRate = 8000;
                double duration = 0.15; // giây
                int length = (int) (sampleRate * duration);
                byte[] buffer = new byte[length];

                for (int i = 0; i < length; i++) {
                    double angle = 2.0 * Math.PI * i * 880.0 / sampleRate;
                    // Bổ sung hiệu ứng giảm âm dần (fade out) để nghe không bị gắt
                    double volume = Math.pow(1.0 - ((double) i / length), 2);
                    buffer[i] = (byte) (Math.sin(angle) * 127 * volume);
                }

                line.write(buffer, 0, buffer.length);
                line.drain();
                line.close();
            } catch (Exception e) {
                // Bỏ qua nếu máy không hỗ trợ âm thanh
                System.err.println("Không thể phát âm thanh: " + e.getMessage());
            }
        }).start();
    }
}
