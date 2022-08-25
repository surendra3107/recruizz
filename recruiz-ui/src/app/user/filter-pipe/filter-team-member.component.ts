import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'memberFilter',
  pure: false
})
export class TeamMemberFilterPipe implements PipeTransform {
  transform(items: any[], searchText: string): any[] {
    if (!items) return [];
    if (!searchText) return items;
    searchText = searchText.toLowerCase();
    return items.filter(it => {
      return it.userName.toLowerCase().includes(searchText);
    });
  }
}
