import type { AppLanguage } from '../model/musicoletModels';

export interface MorontoTranslations {
  brandName: string;
  continueListening: string;
  resumePlayback: string;
  pauseTrack: string;
  openStudioPlayer: string;
  eqAndCrossfade: string;
  importAudio: string;
  tracksTab: string;
  queuesTab: string;
  playlistsTab: string;
  guideTab: string;
  liveTab: string;
  upgradeTab: string;
  storageTab: string;
  searchPlaceholder: string;
  studioTemplatesHeading: string;
  smartGenresHeading: string;
  shareWhatsAppStatus: string;
  liveComingSoonHeading: string;
  userGuideHeading: string;
  proUpgradeHeading: string;
  restorePurchases: string;
}

export const TRANSLATIONS: Record<AppLanguage, MorontoTranslations> = {
  en: {
    brandName: 'Moronto Music',
    continueListening: 'Continue listening',
    resumePlayback: 'Resume Playback',
    pauseTrack: 'Pause Track',
    openStudioPlayer: 'Open Studio Player',
    eqAndCrossfade: '10-Band EQ & Crossfade',
    importAudio: 'Import Audio',
    tracksTab: 'Tracks',
    queuesTab: 'Queues',
    playlistsTab: 'Playlists',
    guideTab: 'User Guide',
    liveTab: 'Live · Coming Soon',
    upgradeTab: 'Pro · Coming Soon',
    storageTab: 'Storage',
    searchPlaceholder: 'Search offline tracks, artists, albums, or folders',
    studioTemplatesHeading: 'One-Click Studio Templates',
    smartGenresHeading: 'Genre Smart Playlists',
    shareWhatsAppStatus: 'Share Card',
    liveComingSoonHeading: 'Live Streaming — Coming Soon',
    userGuideHeading: 'How to Use Moronto Music',
    proUpgradeHeading: 'Moronto Music Pro — Coming Soon (Global Catalog Streaming)',
    restorePurchases: 'Restore Purchases',
  },
  pcm: {
    brandName: 'Moronto Music',
    continueListening: 'Continue to dey listen',
    resumePlayback: 'Play Am Back',
    pauseTrack: 'Pause Song',
    openStudioPlayer: 'Open Studio Player',
    eqAndCrossfade: '10-Band EQ & Fade',
    importAudio: 'Add Songs',
    tracksTab: 'Songs',
    queuesTab: 'Queues',
    playlistsTab: 'Playlists',
    guideTab: 'How To Use',
    liveTab: 'Live · Dey Come',
    upgradeTab: 'Pro · Dey Come',
    storageTab: 'Storage',
    searchPlaceholder: 'Find your offline songs, artists, or folders',
    studioTemplatesHeading: 'Sharp Sound Templates',
    smartGenresHeading: 'Smart Genre Playlists',
    shareWhatsAppStatus: 'Share Card',
    liveComingSoonHeading: 'Live Streaming — E Dey Come Soon',
    userGuideHeading: 'How To Use Moronto Music',
    proUpgradeHeading: 'Moronto Music Pro — E Dey Come (Global Music Streaming)',
    restorePurchases: 'Bring Back Purchase',
  },
  yo: {
    brandName: 'Moronto Music',
    continueListening: 'Tẹ̀síwájú láti gbọ́',
    resumePlayback: 'Bẹ̀rẹ̀ Orin',
    pauseTrack: 'Dúró Díẹ̀',
    openStudioPlayer: 'Ṣí Studio Player',
    eqAndCrossfade: '10-Band EQ & Crossfade',
    importAudio: 'Fi Orin Kún',
    tracksTab: 'Àwọn Orin',
    queuesTab: 'Àwọn Queue',
    playlistsTab: 'Playlist',
    guideTab: 'Ìtọ́sọ́nà',
    liveTab: 'Live · Ń Bọ̀',
    upgradeTab: 'Pro · Ń Bọ̀',
    storageTab: 'Ìpamọ́',
    searchPlaceholder: 'Wá orin, olórin, àwo-orin, tàbí folda',
    studioTemplatesHeading: 'Àwọn Àdàkọ Studio',
    smartGenresHeading: 'Àkójọ Orin Ọlọ́gbọ́n',
    shareWhatsAppStatus: 'Pín Káàdì',
    liveComingSoonHeading: 'Ìgbóhùnsáfẹ́fẹ́ Live — Ń Bọ̀ Láìpẹ́',
    userGuideHeading: 'Bí a ṣe ń lo Moronto Music',
    proUpgradeHeading: 'Moronto Music Pro — Ń Bọ̀ (Ìṣànwọlé Orin Àgbáyé)',
    restorePurchases: 'Mú Rírà Padà',
  },
  ig: {
    brandName: 'Moronto Music',
    continueListening: "Gaa n'ihu na-ege ntị",
    resumePlayback: 'Kpọọ Egwu',
    pauseTrack: 'Kwụsị Ntakịrị',
    openStudioPlayer: 'Mepee Studio Player',
    eqAndCrossfade: '10-Band EQ & Crossfade',
    importAudio: 'Tinye Egwu',
    tracksTab: 'Egwu Niile',
    queuesTab: 'Queue',
    playlistsTab: 'Playlist',
    guideTab: 'Ntụziaka',
    liveTab: 'Live · Na-abịa',
    upgradeTab: 'Pro · Na-abịa',
    storageTab: 'Nchekwa',
    searchPlaceholder: 'Chọọ egwu, onye na-abụ abụ, ma ọ bụ folda',
    studioTemplatesHeading: 'Ụdị Studio Dị Mfe',
    smartGenresHeading: 'Nchịkọta Egwu Smart',
    shareWhatsAppStatus: 'Kekọrịta Kaadị',
    liveComingSoonHeading: 'Mgbasaozi Live — Na-abịa N’oge Adịghị Anya',
    userGuideHeading: 'Otu E Si Eji Moronto Music',
    proUpgradeHeading: 'Moronto Music Pro — Na-abịa (Nkwanye Egwu Ụwa Niile)',
    restorePurchases: 'Weghachite Azụmahịa',
  },
  ha: {
    brandName: 'Moronto Music',
    continueListening: 'Ci gaba da sauraro',
    resumePlayback: 'Kunna Waƙa',
    pauseTrack: 'Dakata',
    openStudioPlayer: 'Buɗe Studio Player',
    eqAndCrossfade: '10-Band EQ & Crossfade',
    importAudio: 'Shigo da Waƙa',
    tracksTab: 'Waƙoƙi',
    queuesTab: 'Jerin Waƙa',
    playlistsTab: 'Playlists',
    guideTab: 'Jagora',
    liveTab: 'Live · Yana Zuwa',
    upgradeTab: 'Pro · Yana Zuwa',
    storageTab: 'Ma’ajiya',
    searchPlaceholder: 'Nemo waƙoƙi, mawaƙa, kundin waƙa, ko babban fayil',
    studioTemplatesHeading: 'Samfuran Studio Masu Sauri',
    smartGenresHeading: 'Jerin Waƙoƙin Nau’i',
    shareWhatsAppStatus: 'Raba Kati',
    liveComingSoonHeading: 'Live Streaming — Yana Zuwa Nan Ba Da Jimawa Ba',
    userGuideHeading: 'Yadda Ake Amfani da Moronto Music',
    proUpgradeHeading: 'Moronto Music Pro — Yana Zuwa (Yaɗa Kiɗan Duniya)',
    restorePurchases: 'Dawo da Siyayya',
  },
};

export function t(lang: AppLanguage): MorontoTranslations {
  return TRANSLATIONS[lang] ?? TRANSLATIONS.en;
}
