package com.example.temp.machine.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.temp.admin.dto.request.BodyPartCreateRequest;
import com.example.temp.admin.dto.request.MachineCreateRequest;
import com.example.temp.common.exception.ApiException;
import com.example.temp.common.exception.ErrorCode;
import com.example.temp.machine.domain.BodyPart;
import com.example.temp.machine.domain.Machine;
import com.example.temp.machine.dto.response.BodyPartCreateResponse;
import com.example.temp.machine.dto.response.MachineCreateResponse;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MachineServiceTest {

    @Autowired
    MachineService machineService;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("신체부위를 등록한다.")
    void createBodyPart() throws Exception {
        // given
        String name = "등";
        BodyPartCreateRequest request = new BodyPartCreateRequest(name);

        // when
        BodyPartCreateResponse response = machineService.createBodyPart(request);
        em.flush();
        em.clear();

        // then
        BodyPart bodyPart = em.find(BodyPart.class, response.id());
        assertThat(bodyPart.getName()).isEqualTo(name);
        assertThat(response.name()).isEqualTo(name);
    }

    @Test
    @DisplayName("이미 등록된 신체부위를 또 등록할 수 없다.")
    void createBodyPartFailDuplicatedName() throws Exception {
        // given
        String name = "등";
        saveBodyPart(name);
        BodyPartCreateRequest request = new BodyPartCreateRequest(name);

        // when
        Assertions.assertThatThrownBy(() -> machineService.createBodyPart(request))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining(ErrorCode.BODY_PART_ALREADY_REGISTER.getMessage());
    }

    @Test
    @DisplayName("운동기구를 등록한다.")
    void createMachine() throws Exception {
        // given
        String name = "벤치프레스";
        BodyPart bodyPart = saveBodyPart("등");
        MachineCreateRequest request = new MachineCreateRequest(name, List.of(bodyPart.getName()));

        // when
        MachineCreateResponse result = machineService.createMachine(request);

        // then
        em.flush();
        em.clear();
        Machine createdMachine = em.find(Machine.class, result.id());

        assertThat(createdMachine.getName()).isEqualTo(name);
        assertThat(createdMachine.getMachineBodyParts()).hasSize(1);
        assertThat(createdMachine.getMachineBodyParts().get(0).getBodyPart().getName()).isEqualTo(bodyPart.getName());
    }

    @Test
    @DisplayName("운동 기구는 한 개가 넘는 신체 부위에 매핑할 수 없다.")
    void machineMappedOnlyOneBodyPart() throws Exception {
        // given
        String name = "벤치프레스";
        BodyPart bodyPart1 = saveBodyPart("등");
        BodyPart bodyPart2 = saveBodyPart("어깨");
        MachineCreateRequest request = new MachineCreateRequest(name,
            List.of(bodyPart1.getName(), bodyPart2.getName()));

        // when & then
        assertThatThrownBy(() -> machineService.createMachine(request))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining(ErrorCode.MACHINE_MATCH_ONLY_ONE_BODY_PART.getMessage());
    }

    @Test
    @DisplayName("머신은 존재하지 않는 신체 부위에 매핑할 수 없다.")
    void machineMappedFailBodyPartNotFound() throws Exception {
        // given
        String name = "벤치프레스";
        String notExistBodyPartValue = "등ㅇ";
        MachineCreateRequest request = new MachineCreateRequest(name,
            List.of(notExistBodyPartValue));

        // when & then
        assertThatThrownBy(() -> machineService.createMachine(request))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining(ErrorCode.MACHINE_MAPPED_INVALID_BODY_PART.getMessage());
    }

    @Test
    @DisplayName("이미 등록된 운동기구를 또 등록할 수 없다.")
    void createMachineFailDuplicatedName() throws Exception {
        // given
        String name = "벤치프레스";
        saveMachine(name);
        MachineCreateRequest request = new MachineCreateRequest(name, List.of("등"));

        // when & then
        assertThatThrownBy(() -> machineService.createMachine(request))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining(ErrorCode.MACHINE_ALREADY_REGISTER.getMessage());
    }

    private Machine saveMachine(String name) {
        Machine machine = Machine.builder()
            .name(name)
            .build();
        em.persist(machine);
        return machine;
    }

    private BodyPart saveBodyPart(String name) {
        BodyPart bodyPart = BodyPart.builder()
            .name(name)
            .build();
        em.persist(bodyPart);
        return bodyPart;
    }

}