package com.example.wallet.modules.wallet;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wallet.modules.balance.Balance;
import com.example.wallet.modules.event.Event;
import com.example.wallet.modules.wallet.dto.CreateWalletDto;
import com.example.wallet.modules.wallet.dto.TransferDto;
import com.example.wallet.utils.DataTypeUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/wallets")
public class WalletController {
  private final WalletService walletService;

  public WalletController(WalletService walletService) {
    this.walletService = walletService;
  }

  /**
   * Create a new wallet
   * @param createWalletDto
   * @param bindingResult
   * @return
   */
  @PostMapping
  public ResponseEntity<String> create(@RequestBody CreateWalletDto createWalletDto) {
    // TODO:Kyuhyeon - apply validator
    if (createWalletDto.getPassword() == null) {
      return ResponseEntity.badRequest().body("Password is required");
    }
    return walletService.create(createWalletDto.getPassword());
  }

  /**
   * Get all wallets
   * @return List of wallets
   */
  @GetMapping
  public ResponseEntity<List<Wallet>> findAll() {
    List<Wallet> wallets = walletService.findAll();
    return ResponseEntity.ok(wallets);
  }

  /**
   * Get events of wallet by address
   * @param request HttpServletRequest
   * @param address Wallet address
   * @return
   */
  @GetMapping("/{address}/events")
  public ResponseEntity<Page<Event>> getEventsByAddress(HttpServletRequest request, @PathVariable("address") String address) {
    Optional<Long> startingAfter = Optional.of(DataTypeUtil.parseLong(request.getParameter("starting_after")));
    Optional<Long> endingBefore = Optional.of(DataTypeUtil.parseLong(request.getParameter("ending_before")));
    Optional<Integer> size = Optional.of(DataTypeUtil.parseInt(request.getParameter("size")));

    return walletService.getEventsByAddress(address, startingAfter, endingBefore, size);
  }

  /**
   * Get balance of wallet by address
   * @param request HttpServletRequest
   * @param address Wallet address
   * @return  
   */
  @GetMapping("/{address}/balance")
  public ResponseEntity<Balance> getBalance(HttpServletRequest request, @PathVariable("address") String address) {
    String networkName = request.getParameter("network_name");

    return walletService.getBalance(address, networkName);
  }

  /**
   * Transfer from wallet
   * @param address Wallet address
   * @param trasferDto TransferDto
   * @param bindingResult BindingResult
   * @return Transaction hash
   */
  @PostMapping("/{address}/transfer")
  public ResponseEntity<String> transfer(@PathVariable String address, @RequestBody TransferDto trasferDto) {
    // TODO:Kyuhyeon - apply validator
    if (trasferDto.getTo() == null) {
      return ResponseEntity.badRequest().body("To is required");
    }
    if (trasferDto.getAmount() == null) {
      return ResponseEntity.badRequest().body("Amount is required");
    }
    if (trasferDto.getPassword() == null) {
      return ResponseEntity.badRequest().body("Password is required");
    }
    if (trasferDto.getNetworkName() == null) {
      return ResponseEntity.badRequest().body("Network name is required");
    }
    return walletService.transfer(trasferDto.getPassword(), trasferDto.getNetworkName(), address, trasferDto.getTo(), new BigInteger(trasferDto.getAmount()) );
  }
}
