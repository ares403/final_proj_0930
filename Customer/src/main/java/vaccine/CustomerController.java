package vaccine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerController {

 @Autowired
 CustomerRepository customerRepository;

 //@ApiOperation(value = "고객 리스트 가져오기")
 @GetMapping("/list")
 public ResponseEntity<List<Customer>> getCustomerList() {
  List<Customer> customers = (List<Customer>) customerRepository.findAll();
  return ResponseEntity.ok(customers);
 }

 //@ApiOperation(value = "고객 1명 가져오기")
 @GetMapping("/get/{id}")
 public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
  Customer customer = customerRepository.findById(id).orElseThrow(null);
  return ResponseEntity.ok(customer);
 }

 //@ApiOperation(value = "상품 예약하기")
 @PostMapping("/reserve")
 public ResponseEntity<Customer> reserveVaccine(@RequestBody Customer customer) {
  customer.setReserveStatus("OK");
  Customer savedCustomer = customerRepository.save(customer);
  return ResponseEntity.ok(savedCustomer);
 }

 //@ApiOperation(value = "예약 취소하기")
 @PatchMapping("/cancel/{id}")
 public ResponseEntity<Customer> cancelReservation(@PathVariable Long id) {
  Customer customer = customerRepository.findById(id).orElseThrow(null);
  //DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  //String dateStr = format.format(Calendar.getInstance().getTime());
  //reservation.setDate(dateStr);
  customer.setReserveStatus("NO");
  Customer canceledReservation = customerRepository.save(customer);
  return ResponseEntity.ok(canceledReservation);
 }

 @PatchMapping("/deleteall")
 public ResponseEntity<String> deleteAll() {
  customerRepository.deleteAll();
  return ResponseEntity.ok("DELETED");
 }

 // CPU 부하 코드
 @GetMapping("/hpa")
 public String testHPA() {
  double x = 0.0001;
  String hostname = "";
  for (int i = 0; i <= 1000000; i++) {
   x += java.lang.Math.sqrt(x);
  }
  try {
   hostname = java.net.InetAddress.getLocalHost().getHostName();
  } catch (java.net.UnknownHostException e) {
   e.printStackTrace();
  }

  return "====== HPA Test(" + hostname + ") ====== \n";
 }
}