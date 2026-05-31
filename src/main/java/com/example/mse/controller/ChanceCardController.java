package com.example.mse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.mse.dto.ApiResponse;
import com.example.mse.dto.ChanceCardUseRequest;
import com.example.mse.dto.ChanceCardUseResponse;
import com.example.mse.model.GameRoom;
import com.example.mse.service.ChanceCardService;
import com.example.mse.service.RoomService;

@RestController
@RequestMapping("/chance")
@CrossOrigin(origins = "*")
public class ChanceCardController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private ChanceCardService chanceCardService;

    @PostMapping("/use")
    public ApiResponse<ChanceCardUseResponse> useChanceCard(
            @RequestBody ChanceCardUseRequest request) {

        GameRoom room = roomService.requireRoom(request.getRoomId());

        ChanceCardUseResponse response = chanceCardService.useChanceCard(
                room,
                request.getPlayerId(),
                request.getCard());

        return ApiResponse.ok("Chance card used.", response);
    }
}