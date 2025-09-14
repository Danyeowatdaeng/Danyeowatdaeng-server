package com.tourapi.tourapi.petAvatar.config;

import com.tourapi.tourapi.petAvatar.PetAvatar;
import com.tourapi.tourapi.petAvatar.enums.PetAvatarStyle;
import com.tourapi.tourapi.petAvatar.enums.PetType;
import com.tourapi.tourapi.petAvatar.repository.PetAvatarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PetAvatarDataInitializer implements CommandLineRunner {

    private final PetAvatarRepository petAvatarRepository;

    @Override
    public void run(String... args) throws Exception {
        // 이미 데이터가 있는지 확인
        if (petAvatarRepository.count() > 0) {
            log.info("PetAvatar 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        log.info("PetAvatar 더미 데이터 초기화를 시작합니다...");

        // 강아지 기본 아바타
        PetAvatar dogAvatar = PetAvatar.createDefaultWithS3(
                PetType.DOG,
                "DOG_DEFAULT_001",
                "귀여운 강아지",
                "avatars/dog/default_001.png",
                "avatars/dog/thumb_001.png",
                "https://cdn.example.com/avatars/dog/default_001.png",
                "image/png",
                512,
                512
        );
        dogAvatar.setAsPrimary();
        petAvatarRepository.save(dogAvatar);

        // 고양이 기본 아바타
        PetAvatar catAvatar = PetAvatar.createDefaultWithS3(
                PetType.CAT,
                "CAT_DEFAULT_001",
                "사랑스러운 고양이",
                "avatars/cat/default_001.png",
                "avatars/cat/thumb_001.png",
                "https://cdn.example.com/avatars/cat/default_001.png",
                "image/png",
                512,
                512
        );
        petAvatarRepository.save(catAvatar);

        // 새 기본 아바타
        PetAvatar birdAvatar = PetAvatar.createDefaultWithS3(
                PetType.BIRD,
                "BIRD_DEFAULT_001",
                "예쁜 새",
                "avatars/bird/default_001.png",
                "avatars/bird/thumb_001.png",
                "https://cdn.example.com/avatars/bird/default_001.png",
                "image/png",
                512,
                512
        );
        petAvatarRepository.save(birdAvatar);

        // 물고기 기본 아바타
        PetAvatar fishAvatar = PetAvatar.createDefaultWithS3(
                PetType.FISH,
                "FISH_DEFAULT_001",
                "반짝이는 물고기",
                "avatars/fish/default_001.png",
                "avatars/fish/thumb_001.png",
                "https://cdn.example.com/avatars/fish/default_001.png",
                "image/png",
                512,
                512
        );
        petAvatarRepository.save(fishAvatar);

        // 토끼 기본 아바타
        PetAvatar rabbitAvatar = PetAvatar.createDefaultWithS3(
                PetType.RABBIT,
                "RABBIT_DEFAULT_001",
                "깡총 토끼",
                "avatars/rabbit/default_001.png",
                "avatars/rabbit/thumb_001.png",
                "https://cdn.example.com/avatars/rabbit/default_001.png",
                "image/png",
                512,
                512
        );
        petAvatarRepository.save(rabbitAvatar);

        // 햄스터 기본 아바타
        PetAvatar hamsterAvatar = PetAvatar.createDefaultWithS3(
                PetType.HAMSTER,
                "HAMSTER_DEFAULT_001",
                "통통 햄스터",
                "avatars/hamster/default_001.png",
                "avatars/hamster/thumb_001.png",
                "https://cdn.example.com/avatars/hamster/default_001.png",
                "image/png",
                512,
                512
        );
        petAvatarRepository.save(hamsterAvatar);

        // 거북이 기본 아바타
        PetAvatar turtleAvatar = PetAvatar.createDefaultWithS3(
                PetType.TURTLE,
                "TURTLE_DEFAULT_001",
                "느긋한 거북이",
                "avatars/turtle/default_001.png",
                "avatars/turtle/thumb_001.png",
                "https://cdn.example.com/avatars/turtle/default_001.png",
                "image/png",
                512,
                512
        );
        petAvatarRepository.save(turtleAvatar);

        // 페럿 기본 아바타
        PetAvatar ferretAvatar = PetAvatar.createDefaultWithS3(
                PetType.FERRET,
                "FERRET_DEFAULT_001",
                "장난꾸러기 페럿",
                "avatars/ferret/default_001.png",
                "avatars/ferret/thumb_001.png",
                "https://cdn.example.com/avatars/ferret/default_001.png",
                "image/png",
                512,
                512
        );
        petAvatarRepository.save(ferretAvatar);

        // 추가 스타일 아바타들 (애니메이션 스타일)
        PetAvatar dogAnimeAvatar = PetAvatar.builder()
                .pet(PetType.DOG)
                .code("DOG_ANIME_001")
                .displayName("애니메이션 강아지")
                .resultKey("avatars/dog/anime_001.png")
                .thumbKey("avatars/dog/anime_thumb_001.png")
                .cdnUrl("https://cdn.example.com/avatars/dog/anime_001.png")
                .imageMime("image/png")
                .width(512)
                .height(512)
                .primary(false)
                .version(1)
                .isActive(true)
                .isCustom(false)
                .style(PetAvatarStyle.ANIME)
                .build();
        petAvatarRepository.save(dogAnimeAvatar);

        // 카툰 스타일 고양이
        PetAvatar catCartoonAvatar = PetAvatar.builder()
                .pet(PetType.CAT)
                .code("CAT_CARTOON_001")
                .displayName("카툰 고양이")
                .resultKey("avatars/cat/cartoon_001.png")
                .thumbKey("avatars/cat/cartoon_thumb_001.png")
                .cdnUrl("https://cdn.example.com/avatars/cat/cartoon_001.png")
                .imageMime("image/png")
                .width(512)
                .height(512)
                .primary(false)
                .version(1)
                .isActive(true)
                .isCustom(false)
                .style(PetAvatarStyle.CARTOON)
                .build();
        petAvatarRepository.save(catCartoonAvatar);

        log.info("PetAvatar 더미 데이터 초기화가 완료되었습니다. 총 {}개의 아바타가 생성되었습니다.", petAvatarRepository.count());
    }
}
