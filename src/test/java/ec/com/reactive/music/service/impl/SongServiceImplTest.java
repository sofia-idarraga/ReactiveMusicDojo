package ec.com.reactive.music.service.impl;

import ec.com.reactive.music.domain.dto.SongDTO;
import ec.com.reactive.music.domain.entities.Song;
import ec.com.reactive.music.repository.ISongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SongServiceImplTest {

    @Mock
    ISongRepository songRepository;

    ModelMapper modelMapper;

    SongServiceImpl songService;

    @BeforeEach
    void init(){
        modelMapper = new ModelMapper();
        songService = new SongServiceImpl(songRepository,modelMapper);
    }

    @Test
    @DisplayName("findAllSongs")
    void findAllSongs() {

        ArrayList<Song> songs = new ArrayList<>(List.of(
                        new Song(), new Song()
                ));

        ArrayList<SongDTO> songsDTO = songs.stream().map(song -> modelMapper.map(song,SongDTO.class)).collect(Collectors.toCollection(ArrayList::new));

        var fluxResult = Flux.fromIterable(songs);
        var fluxResultDTO = Flux.fromIterable(songsDTO);

        ResponseEntity<Flux<SongDTO>> responseEntity = new ResponseEntity<>(fluxResultDTO, HttpStatus.FOUND);

        Mockito.when(songRepository.findAll()).thenReturn(fluxResult);

       var service = songService.findAllSongs();

        StepVerifier.create(service)
                .expectNextMatches(fluxResponseEntity -> fluxResponseEntity.getStatusCode().is3xxRedirection())
                .expectComplete().verify();
    }

    @Test
    @DisplayName("findAllSongsError")
    void findAllSongsError(){


        Mockito.when(songRepository.findAll()).thenReturn(Flux.empty());

        var service = songService.findAllSongs();

        //Is a 2xx because HTTP.NO_CONTENT is "204 No Content"
        StepVerifier.create(service)
                .expectNextMatches(fluxResponseEntity -> fluxResponseEntity.getStatusCode().is2xxSuccessful())
                .expectComplete().verify();
    }

    @Test
    @DisplayName("findSongById")
    void findSongById() {
        Song songExpected = new Song(
                "1111-1",
                "songTest",
                "0000-1",
                "lyrics",
                "productorTest",
                "arrengerTest",
                LocalTime.of(0,3,50)
        );

        SongDTO songExpectedDTO = modelMapper.map(songExpected,SongDTO.class);

        ResponseEntity<SongDTO> songDTOResponseEntity = new ResponseEntity<>(songExpectedDTO, HttpStatus.FOUND);

        Mockito.when(songRepository.findById(Mockito.any(String.class))).thenReturn(Mono.just(songExpected));

        var service = songService.findSongById("1111-1");

        StepVerifier.create(service)
                .expectNext(songDTOResponseEntity)
                .expectComplete().verify();
        Mockito.verify(songRepository).findById("1111-1");

    }

    @Test
    @DisplayName("findSongByIdError")
    void findSongByIdError(){

        ResponseEntity<SongDTO> songDTOResponseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        Mockito.when(songRepository.findById(Mockito.any(String.class))).thenReturn(Mono.empty());

        var service = songService.findSongById("-");

        StepVerifier.create(service)
                .expectNext(songDTOResponseEntity)
                .expectComplete().verify();
        Mockito.verify(songRepository).findById(Mockito.any(String.class));
    }

    @Test
    @DisplayName("saveSong")
    void saveSong() {
        Song songExpected = new Song(
                "1111-1",
                "songTest",
                "0000-1",
                "lyrics",
                "productorTest",
                "arrengerTest",
                LocalTime.of(0,3,50)
        );

        SongDTO songExpectedDTO = modelMapper.map(songExpected,SongDTO.class);
        ResponseEntity<SongDTO> songDTOResponseEntity = new ResponseEntity<>(songExpectedDTO, HttpStatus.CREATED);

        Mockito.when(songRepository.save(Mockito.any(Song.class))).thenReturn(Mono.just(songExpected));

        var service = songService.saveSong(songExpectedDTO);

        StepVerifier.create(service)
                .expectNext(songDTOResponseEntity)
                .expectComplete()
                .verify();

        Mockito.verify(songRepository).save(songExpected);
    }

    @Test
    @DisplayName("saveSongError")
    void saveSongError(){

        Song song = new Song(
                "1111-1",
                "songTest",
                "0000-1",
                "lyrics",
                "productorTest",
                "arrengerTest",
                LocalTime.of(0,3,50)
        );

        SongDTO songDTO = modelMapper.map(song,SongDTO.class);


        ResponseEntity<SongDTO> songDTOResponseEntity = new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        Mockito.when(songRepository.save(Mockito.any(Song.class))).thenReturn(Mono.empty());

        var service = songService.saveSong(songDTO);

        StepVerifier.create(service)
                .expectNext(songDTOResponseEntity)
                .expectComplete().verify();
        Mockito.verify(songRepository).save(Mockito.any(Song.class));
    }

    @Test
    @DisplayName("updateSong")
    void updateSong() {
        Song song = new Song(
                "1111-1",
                "songTest",
                "0000-1",
                "lyrics",
                "productorTest",
                "arrengerTest",
                LocalTime.of(0,3,50)
        );

        var songEdited = song.toBuilder()
                .name("updatedName").lyricsBy("updatedLyrics").build();
        var songEditedDTO = modelMapper.map(songEdited, SongDTO.class);

        ResponseEntity<SongDTO> songDTOResponseEntity = new ResponseEntity<>(songEditedDTO, HttpStatus.ACCEPTED);

        Mockito.when(songRepository.findById(Mockito.any(String.class))).thenReturn(Mono.just(song));
        Mockito.when(songRepository.save(Mockito.any(Song.class))).thenReturn(Mono.just(songEdited));

        var service = songService.updateSong("1111-1", songEditedDTO);

        StepVerifier.create(service)
                .expectNext(songDTOResponseEntity)
                .expectComplete().verify();
    }

    @Test
    @DisplayName("updateSongError")
    void updateSongError(){
        Song song = new Song(
                "1111-1",
                "songTest",
                "0000-1",
                "lyrics",
                "productorTest",
                "arrengerTest",
                LocalTime.of(0,3,50)
        );

        var songEdited = song.toBuilder()
                .name("updatedName").lyricsBy("updatedLyrics").build();
        var songEditedDTO = modelMapper.map(songEdited, SongDTO.class);

        ResponseEntity<SongDTO> songDTOResponseEntity = new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        Mockito.when(songRepository.findById(Mockito.any(String.class))).thenReturn(Mono.empty());

        var service = songService.updateSong("1111-1",songEditedDTO);

        StepVerifier.create(service)
                .expectNext(songDTOResponseEntity)
                .expectComplete().verify();
        Mockito.verify(songRepository).findById(Mockito.any(String.class));
    }

    @Test
    @DisplayName("deleteSong")
    void deleteSong() {
        Song song = new Song(
                "1111-1",
                "songTest",
                "0000-1",
                "lyrics",
                "productorTest",
                "arrengerTest",
                LocalTime.of(0,3,50)
        );

        ResponseEntity<String> responseEntity = new ResponseEntity<>(song.getIdSong(),HttpStatus.ACCEPTED);

        Mockito.when(songRepository.findById(Mockito.any(String.class))).thenReturn(Mono.just(song));
        Mockito.when(songRepository.deleteById(Mockito.any(String.class))).thenReturn(Mono.empty());

        var sercive = songService.deleteSong("1111-1");

        StepVerifier.create(sercive).expectNext(responseEntity).expectComplete().verify();

        Mockito.verify(songRepository).findById(Mockito.any(String.class));
        Mockito.verify(songRepository).deleteById(Mockito.any(String.class));
    }

    @Test
    @DisplayName("deleteSongError")
    void deleteSongError(){

        ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Mockito.when(songRepository.findById(Mockito.any(String.class))).thenReturn(Mono.empty());

        var service = songService.deleteSong("-");

        StepVerifier.create(service).expectNext(responseEntity).expectComplete().verify();
        Mockito.verify(songRepository).findById(Mockito.any(String.class));
    }
}