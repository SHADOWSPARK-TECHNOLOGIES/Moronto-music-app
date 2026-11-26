import { useState } from 'react';
import {
  ArrowDown,
  ArrowUp,
  Layers,
  Play,
  Plus,
  Trash2,
  X,
} from 'lucide-react';
import {
  type MusicoletUiState,
  musicoletStore,
} from '../domain/musicoletStore';
import { MAX_QUEUES } from '../model/musicoletModels';
import { formatDurationMs } from '../utils/format';

interface QueuesViewProps {
  uiState: MusicoletUiState;
}

export function QueuesView({ uiState }: QueuesViewProps) {
  const [newQueueName, setNewQueueName] = useState('');

  const handleCreateQueue = (e: React.FormEvent) => {
    e.preventDefault();
    if (musicoletStore.createQueue(newQueueName)) {
      setNewQueueName('');
    }
  };

  const activeQueue =
    uiState.queues.find((q) => q.id === uiState.activeQueueId) ??
    uiState.queues[0];

  return (
    <div className="space-y-6">
      {/* Header & Create Queue Form */}
      <div className="rounded-2xl bg-[#14243A] border border-[#24344D] p-4 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-base font-bold text-[#12D9A1]">
            Multiple Independent Queues
          </h2>
          <p className="text-xs text-[#A5B6CA] mt-0.5 font-tabular">
            Keep up to {MAX_QUEUES} resumable queues ({uiState.queues.length} /{' '}
            {MAX_QUEUES} active). Each queue saves its exact track and timestamp.
          </p>
        </div>

        <form onSubmit={handleCreateQueue} className="flex items-center gap-2 w-full md:w-auto">
          <input
            type="text"
            value={newQueueName}
            onChange={(e) => setNewQueueName(e.target.value)}
            placeholder="New queue name..."
            aria-label="New queue name"
            className="h-10 px-3.5 rounded-xl bg-[#0B1727] border border-[#24344D] focus:border-[#12D9A1] focus:outline-none text-sm text-[#F4FAFE] placeholder:text-[#A5B6CA] flex-1 min-w-0 sm:w-56"
          />
          <button
            type="submit"
            className="min-h-[40px] px-4 py-2 rounded-xl bg-[#12D9A1] hover:bg-[#10c390] text-[#04130E] text-xs font-bold flex items-center gap-1.5 whitespace-nowrap shrink-0"
          >
            <Plus className="w-4 h-4" aria-hidden="true" />
            <span>New Queue</span>
          </button>
        </form>
      </div>

      {/* Queue Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3.5">
        {uiState.queues.map((queue) => {
          const isCurrent = queue.id === uiState.activeQueueId;
          const currentTrack = uiState.tracks.find(
            (t) => t.id === queue.currentTrackId,
          );
          const pos = isCurrent ? uiState.positionMs : queue.positionMs;

          return (
            <div
              key={queue.id}
              className={`rounded-2xl border p-4 flex flex-col justify-between gap-3 transition-colors ${
                isCurrent
                  ? 'bg-[#1B2F4D] border-[#12D9A1]'
                  : 'bg-[#14243A] border-[#24344D]'
              }`}
            >
              <div>
                <div className="flex items-center justify-between gap-2">
                  <div className="flex items-center gap-2 min-w-0">
                    <Layers
                      className={`w-4 h-4 shrink-0 ${
                        isCurrent ? 'text-[#12D9A1]' : 'text-[#20B6EB]'
                      }`}
                      aria-hidden="true"
                    />
                    <h3 className="text-sm font-bold text-[#F4FAFE] truncate">
                      {queue.name}
                    </h3>
                  </div>
                  <span className="text-xs font-semibold text-[#A5B6CA] font-tabular shrink-0">
                    {queue.trackIds.length}{' '}
                    {queue.trackIds.length === 1 ? 'track' : 'tracks'}
                  </span>
                </div>

                <p className="text-xs text-[#A5B6CA] mt-2 truncate font-tabular">
                  {currentTrack
                    ? `Saved at ${formatDurationMs(pos)} · ${currentTrack.title}`
                    : 'Queue is empty'}
                </p>
              </div>

              <div className="flex items-center justify-between gap-2 pt-1">
                <div className="flex items-center gap-2">
                  {isCurrent ? (
                    <span className="text-xs font-bold text-[#12D9A1] px-2 py-1">
                      Active Queue
                    </span>
                  ) : (
                    <button
                      type="button"
                      onClick={() => musicoletStore.switchQueue(queue.id, false)}
                      className="min-h-[38px] px-3 py-1.5 rounded-xl bg-[#0B1727] border border-[#24344D] hover:border-[#12D9A1] text-xs font-bold text-[#F4FAFE] transition-colors"
                    >
                      Switch &amp; Resume
                    </button>
                  )}

                  <button
                    type="button"
                    disabled={queue.trackIds.length === 0}
                    onClick={() =>
                      isCurrent
                        ? musicoletStore.togglePlayPause()
                        : musicoletStore.switchQueue(queue.id, true)
                    }
                    aria-label={`Play ${queue.name}`}
                    className="min-h-[38px] px-3 py-1.5 rounded-xl bg-[#12D9A1] hover:bg-[#10c390] disabled:opacity-40 text-[#04130E] text-xs font-bold inline-flex items-center gap-1"
                  >
                    <Play className="w-3.5 h-3.5 fill-current" aria-hidden="true" />
                    <span>Play</span>
                  </button>
                </div>

                {uiState.queues.length > 1 && (
                  <button
                    type="button"
                    onClick={() => musicoletStore.deleteQueue(queue.id)}
                    aria-label={`Delete ${queue.name}`}
                    className="min-w-[38px] min-h-[38px] flex items-center justify-center rounded-xl text-[#A5B6CA] hover:text-[#FF6479] hover:bg-[#0B1727]/60"
                  >
                    <Trash2 className="w-4 h-4" aria-hidden="true" />
                  </button>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Active Queue Editor */}
      <div className="rounded-2xl bg-[#0B1727] border border-[#24344D] p-4 space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-bold text-[#20B6EB]">
            Up Next in {activeQueue.name}
          </h3>
          <span className="text-xs text-[#A5B6CA] font-tabular">
            {activeQueue.trackIds.length} tracks
          </span>
        </div>

        {activeQueue.trackIds.length === 0 ? (
          <p className="text-xs text-[#A5B6CA] py-3">
            No tracks in this queue yet. Open the Tracks tab and use the track
            options menu to add songs.
          </p>
        ) : (
          <div className="divide-y divide-[#24344D]/60">
            {activeQueue.trackIds.map((trackId, index) => {
              const track = uiState.tracks.find((t) => t.id === trackId);
              if (!track) return null;
              const isPlayingHere = activeQueue.currentTrackId === track.id;

              return (
                <div
                  key={`${activeQueue.id}-${track.id}-${index}`}
                  className="py-2.5 flex items-center justify-between gap-3"
                >
                  <button
                    type="button"
                    onClick={() =>
                      musicoletStore.playTrackInActiveQueue(track.id, 0)
                    }
                    className="flex-1 min-w-0 text-left flex items-center gap-3"
                  >
                    <span className="w-6 text-xs font-tabular text-[#A5B6CA] text-right shrink-0">
                      {index + 1}
                    </span>
                    <div className="min-w-0 flex-1">
                      <div
                        className={`text-sm font-semibold truncate ${
                          isPlayingHere ? 'text-[#12D9A1]' : 'text-[#F4FAFE]'
                        }`}
                      >
                        {track.title}
                      </div>
                      <div className="text-xs text-[#A5B6CA] truncate">
                        {track.artist} · {formatDurationMs(track.durationMs)}
                      </div>
                    </div>
                  </button>

                  <div className="flex items-center gap-1 shrink-0">
                    <button
                      type="button"
                      disabled={index === 0}
                      onClick={() =>
                        musicoletStore.reorderInQueue(
                          activeQueue.id,
                          index,
                          index - 1,
                        )
                      }
                      aria-label={`Move ${track.title} up in queue`}
                      className="min-w-[36px] min-h-[36px] flex items-center justify-center rounded-lg text-[#A5B6CA] hover:text-[#F4FAFE] disabled:opacity-30"
                    >
                      <ArrowUp className="w-4 h-4" aria-hidden="true" />
                    </button>
                    <button
                      type="button"
                      disabled={index === activeQueue.trackIds.length - 1}
                      onClick={() =>
                        musicoletStore.reorderInQueue(
                          activeQueue.id,
                          index,
                          index + 1,
                        )
                      }
                      aria-label={`Move ${track.title} down in queue`}
                      className="min-w-[36px] min-h-[36px] flex items-center justify-center rounded-lg text-[#A5B6CA] hover:text-[#F4FAFE] disabled:opacity-30"
                    >
                      <ArrowDown className="w-4 h-4" aria-hidden="true" />
                    </button>
                    <button
                      type="button"
                      onClick={() =>
                        musicoletStore.removeFromQueue(activeQueue.id, track.id)
                      }
                      aria-label={`Remove ${track.title} from queue`}
                      className="min-w-[36px] min-h-[36px] flex items-center justify-center rounded-lg text-[#A5B6CA] hover:text-[#FF6479]"
                    >
                      <X className="w-4 h-4" aria-hidden="true" />
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
