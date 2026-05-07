using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WtcConnect.Api.Models;
using WtcConnect.Api.Requests.Customers;
using WtcConnect.Api.Services;

namespace WtcConnect.Api.Controllers

{
    [ApiController]
    [Authorize]
    [Route("customer")]
    public class CustomerController : ControllerBase
    {
        private readonly CustomerService customerService;
        private readonly SegmentService segmentService;

        public CustomerController(CustomerService customerService, SegmentService segmentService)
        {
            this.customerService = customerService;
            this.segmentService = segmentService;
        }

        [HttpPost]
        public async Task<IActionResult> Register([FromBody] CreateCustomerRequest request)
        {
            var customer = request.ToCustomer();

            var existing = await customerService.GetByUserIdAsync(customer.UserId);

            if (existing is not null)
                return BadRequest(new { message = "Customer já existe para este usuário" });

            if (request.SegmentId is not null)
            {
                var segment = await segmentService.GetByIdAsync(request.SegmentId);
                if (segment is null) return BadRequest(new { message = "Segmento não encontrado" });
            }

            await customerService.CreateAsync(customer);

            return Ok(customer);
        }

        [HttpGet("user/id/{userId}")]
        public async Task<IActionResult> GetByUserId([FromRoute] string userId)
        {
            var customer = await customerService.GetByUserIdAsync(userId);
            if (customer is null)
                return NotFound(new { message = "Customer não encontrado para este usuário" });
            return Ok(customer);
        }

        [HttpGet("user/email/{email}")]
        public async Task<IActionResult> GetByUserEmail([FromRoute] string email)
        {
            var customer = await customerService.GetByUserEmailAsync(email);
            if (customer is null)
                return NotFound(new { message = "Customer não encontrado para este usuário" });
            return Ok(customer);
        }

        [HttpGet("segment/{id}")]
        public async Task<IActionResult> GetBySegmentId([FromRoute] string id)
        {
            var customerList = await customerService.GetBySegmentAsync(id);

            return Ok(customerList);
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> GetById([FromRoute] string id)
        {
            var customer = await customerService.GetByIdAsync(id);
            if (customer is null)
                return NotFound(new { message = "Customer não encontrado" });
            return Ok(customer);
        }

        [HttpPatch]
        public async Task<IActionResult> Update([FromBody] UpdateCustomerRequest request)
        {
            var existing = await customerService.GetByIdAsync(request.Id);

            if (existing is null)
                return NotFound(new { message = "Customer não encontrado" });

            var customer = request.ToCustomer(existing);

            await customerService.UpdateAsync(customer);
            return Ok(customer);
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete([FromRoute] string id)
        {
            var customer = await customerService.DeleteAsync(id);

            if (customer is null)
                return NotFound(new { message = "Customer não encontrado" });

            return Ok(customer);
        }
    }
}
