package com.example.temp.record.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.temp.machine.domain.BodyPart;
import com.example.temp.record.domain.SetInTrack;
import com.example.temp.record.domain.Track;
import com.example.temp.record.dto.response.TrackInfoCategorizedBodyPart.SetInfo;
import com.example.temp.record.dto.response.TrackInfoCategorizedBodyPart.TrackSummary;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrackInfoCategorizedBodyPartTest {

    @Test
    @DisplayName("TrackInfoCategorizedBodyPart를 생성한다.")
    void createTrackInfoCategorizedBodyPart() {
        SetInTrack set1 = createNotInitSet(1, 10, 10);
        SetInTrack set2 = createNotInitSet(2, 10, 20);
        Track track = createTrack(List.of(set1, set2), BodyPart.CHEST, "가슴운동기구");

        TrackInfoCategorizedBodyPart result = TrackInfoCategorizedBodyPart.of(BodyPart.CHEST, List.of(track));

        assertThat(result.bodyPart()).isEqualTo(BodyPart.CHEST.getText());
        assertThat(result.tracks()).hasSize(1);
    }

    @Test
    @DisplayName("TrackInfoCategorizedBodyPart를 생성할 때, TrackSummary가 잘 생성되는지 검사한다.")
    void testTrackSummary() {
        SetInTrack set1 = createNotInitSet(1, 10, 10);
        SetInTrack set2 = createNotInitSet(2, 10, 20);
        Track track = createTrack(List.of(set1, set2), BodyPart.CHEST, "가슴운동기구");

        TrackInfoCategorizedBodyPart result = TrackInfoCategorizedBodyPart.of(BodyPart.CHEST, List.of(track));

        TrackSummary trackSummary = result.tracks().get(0);
        assertThat(trackSummary.machineName()).isEqualTo(track.getMachineName());
        assertThat(trackSummary.setInfos()).hasSize(2);
    }

    @Test
    @DisplayName("TrackInfoCategorizedBodyPart를 생성할 때, SetInfo가 잘 생성되는지 검사한다.")
    void testSetInfo() {
        SetInTrack set1 = createNotInitSet(1, 10, 10);
        SetInTrack set2 = createNotInitSet(2, 10, 20);
        Track track = createTrack(List.of(set1, set2), BodyPart.CHEST, "가슴운동기구");

        TrackInfoCategorizedBodyPart result = TrackInfoCategorizedBodyPart.of(BodyPart.CHEST, List.of(track));

        List<SetInfo> setInfos = result.tracks().get(0).setInfos();
        validateSetInfo(setInfos.get(0), set1);
        validateSetInfo(setInfos.get(1), set2);
    }

    private static void validateSetInfo(SetInfo setInfo, SetInTrack set) {
        assertThat(setInfo.order()).isEqualTo(set.getOrder());
        assertThat(setInfo.repeat()).isEqualTo(set.getRepeatCnt());
        assertThat(setInfo.weight()).isEqualTo(set.getWeight());
    }

    private Track createTrack(List<SetInTrack> sets, BodyPart bodyPart, String name) {
        Track track = Track.builder()
            .setsInTrack(sets)
            .majorBodyPart(bodyPart)
            .machineName(name)
            .build();
        for (SetInTrack set : sets) {
            set.relate(track);
        }
        return track;
    }

    private SetInTrack createNotInitSet(int order, int repeat, int weight) {
        return SetInTrack.builder()
            .order(order)
            .repeatCnt(repeat)
            .weight(weight)
            .build();
    }

//    class ExerciseRecordProxy extends ExerciseRecord {
//        private Long id;
//
//        public ExerciseRecordProxy(Long id) {
//            this.id = id;
//        }
//
//        @Override
//        public Long getId() {
//            return id;
//        }
//    }
}