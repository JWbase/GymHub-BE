package com.example.temp.record.dto.response;

import com.example.temp.machine.domain.BodyPart;
import com.example.temp.record.domain.SetInTrack;
import com.example.temp.record.domain.Track;
import java.util.List;

public record TrackInfoCategorizedBodyPart(
    String bodyPart,
    List<TrackSummary> tracks
) {

    public static TrackInfoCategorizedBodyPart of(BodyPart bodyPart, List<Track> tracks) {
        List<TrackSummary> trackSummaries = tracks.stream()
            .map(TrackSummary::from)
            .toList();
        return new TrackInfoCategorizedBodyPart(bodyPart.getText(), trackSummaries);
    }

    public record TrackSummary(
        String machineName,
        List<SetInfo> setInfos
    ) {

        public static TrackSummary from(Track track) {
            List<SetInfo> setInfos = track.getSetsInTrack().stream()
                .map(SetInfo::from)
                .toList();
            return new TrackSummary(track.getMachineName(), setInfos);
        }
    }

    public record SetInfo(
        int order,
        int weight,
        int repeat
    ) {

        public static SetInfo from(SetInTrack setInTrack) {
            return new SetInfo(setInTrack.getOrder(), setInTrack.getWeight(), setInTrack.getRepeatCnt());
        }
    }
}
