using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using WtcConnect.Api.Requests.Segments;
using WtcConnect.Api.Services;

namespace WtcConnect.Api.Controllers;

[ApiController]
[Authorize]
[Route("segment")]
public class SegmentController : ControllerBase
{
    private readonly SegmentService segmentService;

    public SegmentController(SegmentService segmentService)
    {
        this.segmentService = segmentService;
    }

    [HttpPost]
    public async Task<IActionResult> Register([FromBody] CreateSegmentRequest request)
    {
        var existing = await segmentService.GetByNameAsync(request.Name);

        if (existing is not null)
            return BadRequest(new { message = "Segmento já existe" });

        var segment = request.ToSegment();

        await segmentService.CreateAsync(segment);

        return Ok(segment);
    }

    [HttpGet("id/{id}")]
    public async Task<IActionResult> GetById([FromRoute] string id)
    {
        var segment = await segmentService.GetByIdAsync(id);
        if (segment is null)
            return NotFound(new { message = "Segmento não encontrado para este usuário" });

        return Ok(segment);
    }

    [HttpGet("name/{name}")]
    public async Task<IActionResult> GetByName([FromRoute] string name)
    {
        var segment = await segmentService.GetByNameAsync(name);
        if (segment is null)
            return NotFound(new { message = "Segmento não encontrado para este usuário" });

        return Ok(segment);
    }

    [HttpPatch]
    public async Task<IActionResult> Update([FromBody] UpdateSegmentRequest request)
    {
        var existing = await segmentService.GetByIdAsync(request.Id);
        if (existing is null)
            return NotFound(new { message = "Segmento não encontrado" });

        var segment = request.ToSegment(existing);

        await segmentService.UpdateAsync(segment);

        return Ok(segment);
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete([FromRoute] string id)
    {
        var segment = await segmentService.DeleteAsync(id);
        if (segment is null)
            return NotFound(new { message = "Segmento não encontrado" });
        return Ok(segment);
    }
}
