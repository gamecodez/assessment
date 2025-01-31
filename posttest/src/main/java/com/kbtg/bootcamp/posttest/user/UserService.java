package com.kbtg.bootcamp.posttest.user;


import com.kbtg.bootcamp.posttest.exception.BadRequestException;
import com.kbtg.bootcamp.posttest.exception.StatusInternalServerErrorException;
import com.kbtg.bootcamp.posttest.lottery.Lottery;
import com.kbtg.bootcamp.posttest.lottery.LotteryRepository;
import com.kbtg.bootcamp.posttest.payload.LotteryListDetailResponseDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

	private final UserRepository userRepository ;
	private final LotteryRepository lotteryRepository;

	public UserService(UserRepository userRepository, LotteryRepository lotteryRepository) {
		this.userRepository = userRepository;
		this.lotteryRepository = lotteryRepository;
	}
	@Transactional
	public String createUserAndLottery(String userId, String ticketId) {
		Optional<Lottery> optionalLottery = lotteryRepository.findByTicket(ticketId);
		if (optionalLottery.isEmpty() || optionalLottery.get().getAmount() == 0) {
			throw new StatusInternalServerErrorException(
					"Ticket ID : " + ticketId + " is not available.");
		}
		Lottery selectedLottery = optionalLottery.get();
		User userLottery = new User();
		userLottery.setUserId(userId);
		userLottery.setTicketId(selectedLottery);

		User savedUserLottery = userRepository.save(userLottery);
		Integer updatedAmount = selectedLottery.getAmount() - 1;
		selectedLottery.setAmount(updatedAmount);
		lotteryRepository.save(selectedLottery);
		return savedUserLottery.getId().toString();
	}
	public LotteryListDetailResponseDto getUserDetail(String userId) {
		List<User> lotteryListByUserId = userRepository.findByUserId(userId);
		if (lotteryListByUserId.isEmpty()) {
			throw new BadRequestException("User ID : " + userId + " is not found.");
		}
		List<String> lotteryList = lotteryListByUserId.stream().map(User::getTicketId).map(Lottery::getTicket).toList();
		Integer count = lotteryListByUserId.size();
		Integer cost = lotteryListByUserId.stream().map(User::getTicketId).mapToInt(Lottery::getPrice).sum();
		return new LotteryListDetailResponseDto(lotteryList, count, cost);
	}
	@Transactional
	public String sellLotteryByUserIdAndTicketId(String userId, String ticketId) {
		List<User> lotteryListByUserId = userRepository.findByUserId(userId);

		if (lotteryListByUserId.isEmpty()) {
			throw new BadRequestException("User ID : " + userId + " is not bought Ticket ID : " + ticketId);
		}
		Optional<User> optionalLottery = lotteryListByUserId.stream()
				.filter(tempLottery -> Objects.equals(tempLottery
						.getTicketId()
						.getTicket(), ticketId))
				.findAny();
		User userLottery = optionalLottery.get();

		userRepository.delete(userLottery);

		return ticketId;
	}
}


