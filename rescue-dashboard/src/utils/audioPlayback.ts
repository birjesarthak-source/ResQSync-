const encodeWav = (channels: Float32Array[], sampleRate: number): ArrayBuffer => {
  const channelCount = channels.length;
  const sampleCount = channels[0]?.length ?? 0;
  if (!channelCount || !sampleCount || !Number.isFinite(sampleRate) || sampleRate <= 0) {
    throw new Error('Decoded audio contains no playable samples.');
  }
  if (channels.some((channel) => channel.length !== sampleCount)) {
    throw new Error('Decoded audio channels have inconsistent lengths.');
  }

  const buffer = new ArrayBuffer(44 + sampleCount * channelCount * 2);
  const view = new DataView(buffer);
  const writeString = (offset: number, value: string) => {
    for (let i = 0; i < value.length; i++) view.setUint8(offset + i, value.charCodeAt(i));
  };
  writeString(0, 'RIFF');
  view.setUint32(4, buffer.byteLength - 8, true);
  writeString(8, 'WAVE');
  writeString(12, 'fmt ');
  view.setUint32(16, 16, true);
  view.setUint16(20, 1, true);
  view.setUint16(22, channelCount, true);
  view.setUint32(24, sampleRate, true);
  view.setUint32(28, sampleRate * channelCount * 2, true);
  view.setUint16(32, channelCount * 2, true);
  view.setUint16(34, 16, true);
  writeString(36, 'data');
  view.setUint32(40, sampleCount * channelCount * 2, true);

  let offset = 44;
  for (let i = 0; i < sampleCount; i++) {
    for (const channel of channels) {
      const sample = Math.max(-1, Math.min(1, channel[i]));
      view.setInt16(offset, sample < 0 ? sample * 0x8000 : sample * 0x7fff, true);
      offset += 2;
    }
  }
  return buffer;
};

export async function decodeAudioBase64ToWav(audioBase64: string): Promise<Blob> {
  const cleanBase64 = audioBase64
    .trim()
    .replace(/^data:audio\/[^;]+;base64,/i, '')
    .replace(/\s/g, '');
  const binaryStr = atob(cleanBase64);
  const bytes = new Uint8Array(binaryStr.length);
  for (let i = 0; i < binaryStr.length; i++) {
    bytes[i] = binaryStr.charCodeAt(i);
  }

  const { default: decodeMp4 } = await import('@audio/decode-mp4');
  const decoded = await decodeMp4(bytes);
  const wav = encodeWav(decoded.channelData, decoded.sampleRate);
  return new Blob([wav], { type: 'audio/wav' });
}
