package com.thirdparty.ticketing.global.waitingsystem.memory.running;

import com.thirdparty.ticketing.domain.waitingsystem.running.RunningRoom;
import com.thirdparty.ticketing.domain.waitingsystem.waiting.WaitingMember;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemoryRunningRoom implements RunningRoom {

    private static final int MAX_MEMORY_RUNNING_ROOM_SIZE = 100;
    private static final int EXPIRED_MINUTE = 5;

    private final ConcurrentMap<Long, ConcurrentMap<String, WaitingMember>> room;

    public boolean contains(String email, long performanceId) {
        if (!room.containsKey(performanceId)) {
            return false;
        }
        return room.get(performanceId).containsKey(email);
    }

    public long getAvailableToRunning(long performanceId) {
        return Math.max(
                0,
                MAX_MEMORY_RUNNING_ROOM_SIZE
                        - (room.containsKey(performanceId) ? room.get(performanceId).size() : 0));
    }

    public void enter(long performanceId, Set<WaitingMember> waitingMembers) {
        room.compute(
                performanceId,
                (key, room) -> {
                    ConcurrentMap<String, WaitingMember> runningRoom =
                            (room != null) ? room : new ConcurrentHashMap<>();
                    waitingMembers.forEach(member -> runningRoom.put(member.getEmail(), member));
                    return runningRoom;
                });
    }

    public void pullOutRunningMember(String email, long performanceId) {
        room.computeIfPresent(
                performanceId,
                (key, room) -> {
                    room.remove(email);
                    return room;
                });
    }

    public void removeExpiredMemberInfo(long performanceId) {
        room.computeIfPresent(performanceId,
                (key, room) -> {
                    ZonedDateTime fiveMinutesAgo = ZonedDateTime.now().minusMinutes(EXPIRED_MINUTE);
                    room.values().stream()
                            .filter(member -> member.getEnteredAt().isBefore(fiveMinutesAgo))
                            .forEach(member -> {
                                room.remove(member.getEmail());
                            });
                    return room;
                });
    }
}
